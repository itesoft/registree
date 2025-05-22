FROM node:latest AS build-stage

WORKDIR /registree-frontend

COPY registree-frontend .

RUN npm ci \
  && npm run build

FROM nginx:alpine AS production-stage

RUN mkdir /app

COPY --from=build-stage /registree-frontend/dist /registree-frontend

COPY dockerroot /
