# LeaveEase Deployment Guide

## Production Deployment

This guide covers deploying LeaveEase to production using Docker Compose with Nginx reverse proxy.

### Prerequisites

- Docker & Docker Compose
- Domain name (optional, for SSL)
- SSL certificates (optional)

### Quick Deployment

1. **Prepare Environment**
```bash
# Copy environment template
cp .env.example .env

# Edit .env with your production values
nano .env
```

2. **Deploy with Docker Compose**
```bash
# Build and start all services
docker-compose -f docker-compose.prod.yml up -d --build

# Initialize database with demo data (optional)
docker-compose -f docker-compose.prod.yml exec backend python app/init_data.py
```

3. **Verify Deployment**
```bash
# Check service status
docker-compose -f docker-compose.prod.yml ps

# Check logs
docker-compose -f docker-compose.prod.yml logs -f
```

### Environment Variables

Key production environment variables:

```bash
# Database
POSTGRES_DB=leavedb
POSTGRES_USER=leaveuser
POSTGRES_PASSWORD=your_secure_password

# Security
SECRET_KEY=your-very-secure-secret-key

# Application
DEBUG=false
ALLOWED_ORIGINS=["https://yourdomain.com"]
```

### SSL Configuration

For HTTPS setup:

1. **Obtain SSL Certificates**
```bash
# Using Let's Encrypt (recommended)
certbot certonly --standalone -d yourdomain.com
```

2. **Update Nginx Configuration**
```nginx
server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    # SSL settings
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;
    
    # ... rest of configuration
}
```

3. **Mount SSL Certificates**
```yaml
volumes:
  - ./ssl:/etc/nginx/ssl:ro
```

### Monitoring and Maintenance

#### Health Checks
- Backend: `GET /health`
- Database: PostgreSQL connection check
- Frontend: Nginx status

#### Logs
```bash
# View all logs
docker-compose -f docker-compose.prod.yml logs

# Follow logs for specific service
docker-compose -f docker-compose.prod.yml logs -f backend

# View recent logs
docker-compose -f docker-compose.prod.yml logs --tail=100
```

#### Backups
```bash
# Database backup
docker-compose -f docker-compose.prod.yml exec postgres pg_dump -U leaveuser leavedb > backup.sql

# Restore database
docker-compose -f docker-compose.prod.yml exec -T postgres psql -U leaveuser leavedb < backup.sql
```

### Scaling

#### Horizontal Scaling
```yaml
# docker-compose.prod.yml
services:
  backend:
    deploy:
      replicas: 3
```

#### Load Balancing
Configure Nginx or use external load balancer for multiple backend instances.

### Security Considerations

1. **Network Security**
   - Use internal networks for service communication
   - Expose only necessary ports
   - Implement firewall rules

2. **Application Security**
   - Change default passwords
   - Use strong SECRET_KEY
   - Enable HTTPS
   - Implement rate limiting

3. **Database Security**
   - Use strong passwords
   - Limit database access
   - Regular backups

### Troubleshooting

#### Common Issues

1. **Database Connection**
```bash
# Check database status
docker-compose -f docker-compose.prod.yml exec postgres pg_isready

# Test connection
docker-compose -f docker-compose.prod.yml exec backend python -c "from app.database import engine; print(engine.url)"
```

2. **API Issues**
```bash
# Test API endpoint
curl -f http://localhost/api/health

# Check backend logs
docker-compose -f docker-compose.prod.yml logs backend
```

3. **Frontend Issues**
```bash
# Check Nginx configuration
docker-compose -f docker-compose.prod.yml exec nginx nginx -t

# Reload Nginx
docker-compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

### Performance Optimization

1. **Database Optimization**
   - Add indexes for frequently queried fields
   - Monitor slow queries
   - Optimize connection pooling

2. **Application Optimization**
   - Enable caching
   - Use CDN for static assets
   - Implement API rate limiting

3. **Infrastructure Optimization**
   - Use SSD storage
   - Allocate sufficient memory
   - Monitor resource usage

### CI/CD Integration

Example GitHub Actions workflow:

```yaml
name: Deploy LeaveEase
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Deploy to production
        run: |
          docker-compose -f docker-compose.prod.yml up -d --build
```

### Rollback Procedure

```bash
# Stop current services
docker-compose -f docker-compose.prod.yml down

# Restore database backup
docker-compose -f docker-compose.prod.yml up -d postgres
docker-compose -f docker-compose.prod.yml exec -T postgres psql -U leaveuser leavedb < backup.sql

# Start previous version
docker-compose -f docker-compose.prod.yml up -d
```

### Support

For deployment issues:
1. Check logs for error messages
2. Verify environment variables
3. Test individual services
4. Review this documentation
5. Check GitHub issues for known problems
