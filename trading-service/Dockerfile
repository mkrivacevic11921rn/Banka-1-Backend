FROM golang:1.24.1-alpine AS builder
WORKDIR /app
COPY . .
RUN go install
RUN go build -o app

FROM alpine:latest
WORKDIR /app
COPY --from=builder /app/.env .env
COPY --from=builder /app/app .
EXPOSE 3000
CMD ["./app"]
            