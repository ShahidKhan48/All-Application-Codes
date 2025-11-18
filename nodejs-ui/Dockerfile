FROM node:22.18.0 as builder

ARG BUILD_ARG=build:prod
ARG START_ARG=start:prod

ENV START_CMD=${START_ARG}
ENV BUILD_CMD=${BUILD_ARG}

WORKDIR /app

# Install dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy the rest of the source code
COPY . .

RUN npm install -f

# Build the app
RUN npm run ${BUILD_CMD}

# Stage 2: Serve the app with a static server
FROM nginx:alpine

# Copy built assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy custom nginx config (for SPA routing)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"] 