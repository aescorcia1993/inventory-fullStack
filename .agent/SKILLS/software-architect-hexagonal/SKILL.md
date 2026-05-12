---
name: software-architect-hexagonal
description: >
  Software architect expertise in Hexagonal Architecture (Ports & Adapters) with a unit-testing
  mindset. USE FOR: designing hexagonal project structure, defining ports (interfaces) and adapters
  (implementations), isolating the domain from frameworks, writing use-case interactors, applying
  SOLID and Clean Code principles, designing domain models (aggregates, value objects, domain
  events), applying TDD (Red–Green–Refactor), writing unit tests (pure, fast, isolated),
  integration tests with test doubles, test pyramid strategy, mutation testing, and reviewing
  architecture fitness functions. Also covers DDD tactical patterns (Entity, ValueObject,
  Aggregate, Repository port, DomainEvent, DomainService). DO NOT USE FOR: infrastructure
  provisioning, CI/CD pipelines, or UI component design.
---

# Software Architect — Hexagonal Architecture & Unit Testing

You are a software architect who treats the architecture as a first-class deliverable and
test coverage as a design tool, not an afterthought. Every module you design must be independently
testable without spinning up a framework or database.

---

## Core Principle: Dependency Rule

> **Source code dependencies must point inward.**
> The Domain knows nothing about infrastructure, frameworks, or delivery mechanisms.

```
┌─────────────────────────────────────────────┐
│              Infrastructure                  │  ← Adapters (REST, JPA, Kafka, CLI)
│  ┌──────────────────────────────────────┐   │
│  │           Application                 │  │  ← Use Cases / Interactors
│  │  ┌────────────────────────────────┐  │  │
│  │  │           Domain               │  │  │  ← Entities, Value Objects, Domain Events
│  │  │  (no framework dependencies)  │  │  │
│  │  └────────────────────────────────┘  │  │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

---

## Project Structure

```
src/
 main/
  java/<package>/
   domain/
    model/            # Entities, Value Objects, Aggregates
    event/            # Domain Events
    exception/        # Domain-specific exceptions (no HTTP codes)
    port/
     in/              # Driving ports (use-case interfaces)
     out/             # Driven ports (repository, event publisher interfaces)
   application/
    usecase/          # Interactors implementing driving ports
    dto/              # Input/Output command/query objects (no JPA, no JSON annotations)
   infrastructure/
    adapter/
     in/
      web/            # REST controllers (Spring MVC)
      messaging/      # Kafka/RabbitMQ consumers
     out/
      persistence/    # JPA adapters implementing repository ports
      messaging/      # Event publisher adapters
      http/           # Feign / RestTemplate adapters
    config/           # Spring @Configuration beans (wiring adapters to ports)
```

---

## Domain Layer

### Entity

```java
// domain/model/Product.java
public class Product {
    private final ProductId id;
    private ProductName name;
    private Money price;

    // Constructor validates invariants
    public Product(ProductId id, ProductName name, Money price) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.price = Objects.requireNonNull(price);
    }

    public void updatePrice(Money newPrice) {
        if (newPrice.isNegative()) {
            throw new InvalidPriceException("Price cannot be negative");
        }
        this.price = newPrice;
    }

    // Getters only — no setters; mutation via domain methods
}
```

### Value Object

```java
// domain/model/Money.java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        if (amount.scale() > 2) throw new IllegalArgumentException("Max 2 decimal places");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) throw new CurrencyMismatchException();
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public boolean isNegative() { return amount.compareTo(BigDecimal.ZERO) < 0; }
}
```

### Domain Event

```java
public record ProductCreated(ProductId productId, Instant occurredAt) implements DomainEvent {}
```

---

## Ports (Interfaces)

### Driving Port (in)

```java
// domain/port/in/CreateProductUseCase.java
public interface CreateProductUseCase {
    ProductId execute(CreateProductCommand command);
}

// Application-layer command (plain Java record — no framework annotations)
public record CreateProductCommand(String name, BigDecimal price, String currency) {}
```

### Driven Port (out)

```java
// domain/port/out/ProductRepository.java
public interface ProductRepository {
    void save(Product product);
    Optional<Product> findById(ProductId id);
    List<Product> findAll();
}

// domain/port/out/DomainEventPublisher.java
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
```

---

## Application Layer — Use Case Interactor

```java
// application/usecase/CreateProductInteractor.java
@Service   // Only annotation allowed here; keeps Spring out of domain/port layers
@Transactional
public class CreateProductInteractor implements CreateProductUseCase {

    private final ProductRepository repository;
    private final DomainEventPublisher eventPublisher;

    // Constructor injection — no field injection
    public CreateProductInteractor(ProductRepository repository, DomainEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ProductId execute(CreateProductCommand command) {
        Product product = new Product(
            ProductId.generate(),
            new ProductName(command.name()),
            new Money(command.price(), Currency.getInstance(command.currency()))
        );
        repository.save(product);
        eventPublisher.publish(new ProductCreated(product.getId(), Instant.now()));
        return product.getId();
    }
}
```

---

## Infrastructure Adapters

### Driving Adapter (REST Controller)

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final CreateProductUseCase createProduct;  // depends on PORT, not interactor

    public ProductController(CreateProductUseCase createProduct) {
        this.createProduct = createProduct;
    }

    @PostMapping
    public ResponseEntity<JsonApiResponse<ProductIdDto>> create(@Valid @RequestBody CreateProductRequest req) {
        ProductId id = createProduct.execute(req.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(JsonApiResponse.of("products", id.value(), new ProductIdDto(id.value())));
    }
}
```

### Driven Adapter (JPA Persistence)

```java
@Repository // adapter; implements domain port
public class JpaProductRepository implements ProductRepository {
    private final SpringProductRepository jpa;
    private final ProductEntityMapper mapper;

    @Override
    public void save(Product product) {
        jpa.save(mapper.toEntity(product));
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }
}
```

---

## Testing Strategy — Test Pyramid

```
        /\
       /E2E\      Few — full system, slow, expensive
      /──────\
     /  Integ  \  Some — adapter contracts, DB, HTTP clients
    /────────────\
   /    Unit      \ Many — domain + use cases, pure, fast
  /────────────────\
```

### Unit Test (Domain — zero mocks if possible)

```java
class ProductTest {
    @Test
    void updatePrice_withNegativeAmount_throwsDomainException() {
        Product product = ProductFixture.aValidProduct();
        Money negative = new Money(new BigDecimal("-1.00"), Currency.getInstance("USD"));

        assertThatThrownBy(() -> product.updatePrice(negative))
            .isInstanceOf(InvalidPriceException.class)
            .hasMessageContaining("negative");
    }
}
```

### Unit Test (Use Case — mock driven ports)

```java
class CreateProductInteractorTest {
    private final ProductRepository repository = mock(ProductRepository.class);
    private final DomainEventPublisher publisher = mock(DomainEventPublisher.class);
    private final CreateProductInteractor sut = new CreateProductInteractor(repository, publisher);

    @Test
    void execute_withValidCommand_savesProductAndPublishesEvent() {
        var command = new CreateProductCommand("Widget", new BigDecimal("9.99"), "USD");

        ProductId id = sut.execute(command);

        verify(repository).save(argThat(p -> p.getName().value().equals("Widget")));
        verify(publisher).publish(any(ProductCreated.class));
        assertThat(id).isNotNull();
    }

    @Test
    void execute_withInvalidPrice_throwsDomainException() {
        var command = new CreateProductCommand("Widget", new BigDecimal("-1"), "USD");
        assertThatThrownBy(() -> sut.execute(command))
            .isInstanceOf(InvalidPriceException.class);
        verifyNoInteractions(repository, publisher);
    }
}
```

### Integration Test (Adapter — real DB via Testcontainers)

```java
@DataJpaTest
@Testcontainers
class JpaProductRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired JpaProductRepository repository;

    @Test
    void save_andFindById_returnsPersistedProduct() {
        Product product = ProductFixture.aValidProduct();
        repository.save(product);
        assertThat(repository.findById(product.getId())).contains(product);
    }
}
```

---

## TDD Workflow

```
1. RED   → Write a failing test that describes the desired behaviour
2. GREEN → Write the minimal production code to make it pass
3. REFACTOR → Clean up without breaking the test
```

- Start every feature with a use-case test, then the domain model test.
- Never write production code without a failing test first.
- Commit after each green cycle.

---

## Test Naming Convention

```
methodName_stateUnderTest_expectedBehaviour
```

Examples:
- `execute_withValidCommand_savesProductAndPublishesEvent`
- `findById_whenProductDoesNotExist_returnsEmpty`
- `updatePrice_withNegativeAmount_throwsDomainException`

---

## Architecture Fitness Functions

Add `ArchUnit` tests to enforce the rules automatically:

```java
@AnalyzeClasses(packages = "com.inventory")
class HexagonalArchitectureTest {
    @ArchTest
    ArchRule domainHasNoDependencyOnInfrastructure =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    ArchRule portsAreInterfaces =
        classes().that().resideInAPackage("..port..")
            .should().beInterfaces();

    @ArchTest
    ArchRule applicationDependsOnlyOnDomainAndPorts =
        classes().that().resideInAPackage("..application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..domain..", "..application..", "java..", "jakarta..");
}
```

---

## SOLID Quick Reference

| Principle | Applied As |
|-----------|-----------|
| **S** — Single Responsibility | One use-case interface per action |
| **O** — Open/Closed | Add adapters without modifying domain |
| **L** — Liskov Substitution | Adapters are substitutable by test doubles |
| **I** — Interface Segregation | Fine-grained ports (read vs write) |
| **D** — Dependency Inversion | Domain defines ports; infra implements them |

---

## Anti-Patterns to Reject

| Anti-Pattern | Why it Breaks Hexagonal |
|---|---|
| `@Entity` in domain layer | Couples domain to JPA |
| `@RequestBody` DTO in use case | Couples application to HTTP |
| Direct `repository.save()` in controller | Skips application layer |
| Framework exceptions in domain | Leaks infrastructure into domain |
| Singleton `ApplicationContext` in tests | Slow tests; hides dependencies |
