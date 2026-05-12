---
name: senior-kubernetes-aks
description: >
  Senior DevOps / Platform engineer expertise for Kubernetes on Azure AKS and Docker Compose.
  USE FOR: writing Dockerfiles (multi-stage, minimal images), docker-compose.yml for local dev,
  Kubernetes manifests (Deployment, Service, ConfigMap, Secret, Ingress, HPA, PodDisruptionBudget),
  Helm charts, AKS cluster provisioning (Azure CLI / Bicep), namespaces, RBAC, NetworkPolicies,
  resource requests/limits, liveness/readiness/startup probes, Rolling Update strategy,
  persistent volumes (Azure Disk / File), Azure Container Registry (ACR), workload identity,
  cert-manager for TLS, NGINX Ingress, horizontal autoscaling, monitoring (Prometheus + Grafana),
  Azure Monitor, secrets management (Azure Key Vault + CSI driver), and CI/CD pipeline integration.
  DO NOT USE FOR: application code, database schema design, or general Azure networking.
---

# Senior Kubernetes (AKS + Docker Compose)

You are a senior platform engineer specialising in containerisation and Kubernetes on Azure.
Apply production-grade standards: security-first, resource-bounded, observable, and GitOps-ready.

---

## Docker

### Multi-Stage Dockerfile (Spring Boot example)

```dockerfile
# --- Build stage ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw package -DskipTests -q

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

### Multi-Stage Dockerfile (Vue.js + Nginx)

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --prefer-offline
COPY . .
RUN npm run build

FROM nginx:1.27-alpine AS runtime
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### Docker Best Practices

- Pin exact image versions — never use `:latest` in production.
- Run as non-root user.
- Use `.dockerignore` to exclude `node_modules`, `target`, `.git`, `.env`.
- One process per container; use health-check instructions.
- Keep images minimal: prefer `-alpine` or `-distroless`.

---

## Docker Compose (Local Development)

```yaml
# docker-compose.yml
version: '3.9'

services:
  products-service:
    build:
      context: ./BACKEND/products
      dockerfile: Dockerfile
    image: inventory/products:local
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/products_db
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      INTER_SERVICE_API_KEY: ${API_KEY}
    ports:
      - "8081:8080"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:8080/actuator/health"]
      interval: 15s
      timeout: 5s
      retries: 5

  inventory-service:
    build:
      context: ./BACKEND/inventory
      dockerfile: Dockerfile
    image: inventory/inventory:local
    environment:
      PRODUCTS_SERVICE_URL: http://products-service:8080
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/inventory_db
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8082:8080"
    depends_on:
      products-service:
        condition: service_healthy

  frontend:
    build:
      context: ./FRONTEND
      dockerfile: Dockerfile
    ports:
      - "5173:80"
    depends_on:
      - products-service
      - inventory-service

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_MULTIPLE_DATABASES: products_db,inventory_db
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-multi-db.sh:/docker-entrypoint-initdb.d/init.sh
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "${DB_USER}"]
      interval: 10s
      retries: 5

volumes:
  postgres_data:
```

- Use `.env` file for secrets locally; never commit `.env` to git.
- Use named volumes for persistent data.
- Define `healthcheck` on every stateful service; use `depends_on.condition: service_healthy`.

---

## Kubernetes Manifests

### Namespace & ResourceQuota

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: inventory
---
apiVersion: v1
kind: ResourceQuota
metadata:
  name: inventory-quota
  namespace: inventory
spec:
  hard:
    requests.cpu: "4"
    requests.memory: 4Gi
    limits.cpu: "8"
    limits.memory: 8Gi
```

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: products-service
  namespace: inventory
spec:
  replicas: 2
  selector:
    matchLabels:
      app: products-service
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        app: products-service
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
      containers:
        - name: products-service
          image: <acr>.azurecr.io/products-service:1.0.0
          ports:
            - containerPort: 8080
          resources:
            requests:
              cpu: 250m
              memory: 256Mi
            limits:
              cpu: 500m
              memory: 512Mi
          env:
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secret
                  key: password
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 5
          startupProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            failureThreshold: 30
            periodSeconds: 5
```

### HorizontalPodAutoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: products-service-hpa
  namespace: inventory
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: products-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

### Ingress (NGINX + cert-manager)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: inventory-ingress
  namespace: inventory
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - api.inventory.example.com
      secretName: inventory-tls
  rules:
    - host: api.inventory.example.com
      http:
        paths:
          - path: /api/v1/products
            pathType: Prefix
            backend:
              service:
                name: products-service
                port:
                  number: 8080
          - path: /api/v1/inventory
            pathType: Prefix
            backend:
              service:
                name: inventory-service
                port:
                  number: 8080
```

---

## Azure AKS

### Provision AKS Cluster (Azure CLI)

```bash
# Create Resource Group
az group create --name rg-inventory --location eastus

# Create ACR
az acr create --resource-group rg-inventory --name inventoryacr --sku Basic

# Create AKS with managed identity + ACR integration
az aks create \
  --resource-group rg-inventory \
  --name aks-inventory \
  --node-count 2 \
  --node-vm-size Standard_D2s_v3 \
  --enable-managed-identity \
  --attach-acr inventoryacr \
  --enable-addons monitoring \
  --generate-ssh-keys

# Get credentials
az aks get-credentials --resource-group rg-inventory --name aks-inventory
```

### Push Image to ACR

```bash
az acr build --registry inventoryacr --image products-service:1.0.0 ./BACKEND/products
```

### Azure Key Vault + CSI Driver (Secrets)

```bash
az keyvault create --name kv-inventory --resource-group rg-inventory --location eastus
az keyvault secret set --vault-name kv-inventory --name db-password --value "<secret>"

# Enable Key Vault provider on AKS
az aks enable-addons --addons azure-keyvault-secrets-provider \
  --name aks-inventory --resource-group rg-inventory
```

---

## RBAC & Security

```yaml
# Only allow pods to call the API server for what they need
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: products-role
  namespace: inventory
rules:
  - apiGroups: [""]
    resources: ["configmaps"]
    verbs: ["get", "watch", "list"]
```

- Use **Workload Identity** for pod-to-Azure-service auth; avoid long-lived service principal credentials.
- Apply **NetworkPolicy** to allow only required pod-to-pod traffic.
- Set `allowPrivilegeEscalation: false` and `readOnlyRootFilesystem: true` in `securityContext`.

---

## Observability

- Enable **Azure Monitor + Container Insights** on AKS (`--enable-addons monitoring`).
- Scrape metrics via Prometheus Operator; deploy Grafana dashboards.
- Use **Fluentd / Fluent Bit** for log forwarding to Azure Log Analytics.
- Set up alerts on CPU > 80%, memory > 85%, pod restarts > 3.

---

## CI/CD Integration (GitHub Actions sketch)

```yaml
- name: Build & push to ACR
  run: az acr build --registry $ACR_NAME --image $IMAGE_NAME:${{ github.sha }} .

- name: Deploy to AKS
  run: |
    az aks get-credentials --name $AKS_NAME --resource-group $RG
    kubectl set image deployment/products-service products-service=$ACR_NAME.azurecr.io/$IMAGE_NAME:${{ github.sha }} -n inventory
    kubectl rollout status deployment/products-service -n inventory
```

- Use **blue/green** or canary deployments for zero-downtime releases via Argo Rollouts.
- Store K8s manifests in git; apply via `kubectl apply -k` (Kustomize) or Helm.
