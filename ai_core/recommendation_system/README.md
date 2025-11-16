# LovAI Recommendation System MVP

An AI-powered recommendation system for dating couples that learns from their habits and preferences to suggest perfect date spots.

## Features

- **Smart Recommendations**: AI-powered suggestions based on dating history and preferences
- **Weather Integration**: Real-time weather data to optimize recommendations
- **Location Intelligence**: Finds nearby venues using geolocation services
- **Behavioral Learning**: Learns from user feedback and actual behavior
- **Push Notifications**: Proactive suggestions via Firebase Cloud Messaging
- **Automated Scheduling**: Background jobs for continuous recommendation generation

## Architecture

### Tech Stack
- **Backend**: FastAPI (Python 3.12)
- **Database**: PostgreSQL with JSONB support
- **Scheduler**: APScheduler for automated jobs
- **External APIs**: Open-Meteo (weather), Geoapify (places)
- **Push Notifications**: Firebase Cloud Messaging
- **Package Manager**: UV

### Core Components

1. **Recommendation Engine**: Scores venues based on affinity, rating, distance, and weather
2. **Learning System**: Analyzes user logs to improve future suggestions
3. **Scheduler**: Automated jobs for preference analysis and recommendation generation
4. **Caching Layer**: PostgreSQL-based caching for external API responses
5. **Notification System**: FCM integration for push notifications

## Scoring Algorithm

The recommendation system uses a weighted scoring formula:

```
score = 0.35×affinity + 0.30×rating + 0.20×distance + 0.15×weather
```

- **Affinity**: Learned preference based on historical visit patterns
- **Rating**: Venue rating normalized to 0-1 scale
- **Distance**: Calculated using `1/(1+distance_km)` formula
- **Weather**: Bonus based on weather conditions and venue type

## Installation

### Prerequisites
- Python 3.12+
- PostgreSQL 12+
- UV package manager

### Setup

1. **Clone and navigate to the project**
```bash
cd /path/to/lovai/recommendation_system
```

2. **Create virtual environment**
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

3. **Install dependencies**
```bash
uv pip install -r requirements.txt
```

4. **Setup database**
```bash
# Create PostgreSQL database
createdb lovai_reco

# Run migrations
cd backend
alembic upgrade head
```

5. **Configure environment**
```bash
# Copy example environment file
cp .env.example .env

# Edit .env with your configuration
# - Database URL
# - API keys (Geoapify, Firebase)
# - Other settings
```

6. **Run the application**
```bash
cd backend
python main.py
```

The API will be available at `http://localhost:8000`

## API Documentation

### Endpoints

- **Health Check**: `GET /api/v1/health`
- **Create Log**: `POST /api/v1/logs`
- **Get Logs**: `GET /api/v1/logs/couple/{couple_id}`
- **Test Notification**: `POST /api/v1/notify/test`
- **Update FCM Token**: `POST /api/v1/notify/fcm-token`
- **Admin Functions**: `GET /api/v1/admin/scheduler/status`

### Interactive Documentation
- Swagger UI: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

## Configuration

### Environment Variables

Key configuration options in `.env`:

```env
# Database
DATABASE_URL=postgresql://user:pass@localhost:5432/lovai_reco

# External APIs
GEOAPIFY_API_KEY=your_api_key
FIREBASE_CREDENTIALS_PATH=/path/to/firebase-creds.json

# Scheduler
SCHEDULER_ENABLED=true
GENERATE_RECO_JOB_INTERVAL_MINUTES=120

# Scoring weights
AFFINITY_WEIGHT=0.35
RATING_WEIGHT=0.30
DISTANCE_WEIGHT=0.20
WEATHER_WEIGHT=0.15
```

## Usage

### 1. Create Users and Couples
Set up users and couple relationships in the database.

### 2. Log Dating Activities
```bash
curl -X POST "http://localhost:8000/api/v1/logs" \
  -H "Content-Type: application/json" \
  -d '{
    "couple_id": 1,
    "started_at": "2024-01-15T19:00:00Z",
    "ended_at": "2024-01-15T22:00:00Z",
    "lat": 37.7749,
    "lng": -122.4194,
    "place_type": "restaurant",
    "place_name": "Amazing Italian Restaurant",
    "spent": 85.50,
    "rating": 4.5,
    "note": "Great pasta and atmosphere"
  }'
```

### 3. Automatic Recommendations
The scheduler automatically:
- Analyzes dating patterns daily
- Generates recommendations every 2 hours
- Sends push notifications
- Learns from user behavior

### 4. Manual Operations
```bash
# Trigger job manually
curl -X POST "http://localhost:8000/api/v1/admin/scheduler/trigger" \
  -H "Content-Type: application/json" \
  -d '{"job_id": "generate_recommendations"}'

# Generate recommendations for specific couple
curl -X POST "http://localhost:8000/api/v1/admin/recommendations/generate/1"
```

## Database Schema

### Key Tables
- **users**: User profiles and preferences
- **couples**: Couple relationships
- **logs**: Dating activity logs
- **recommendations**: Generated recommendations with scoring
- **place_cache** / **weather_cache**: API response caching

### JSONB Fields
- `preferences_jsonb`: User/couple preferences
- `criteria_jsonb`: Recommendation criteria
- `results_jsonb`: Recommendation results with scores
- `engagement_signals_jsonb`: Behavioral feedback

## Development

### Project Structure
```
backend/
├── app/
│   ├── api/          # REST endpoints
│   ├── services/     # Business logic
│   ├── models/       # Database models
│   ├── scoring/      # Recommendation algorithms
│   ├── clients/      # External API clients
│   └── core/         # Configuration and utilities
├── alembic/          # Database migrations
└── main.py           # Application entry point
```

### Adding New Features

1. **New API Endpoint**: Add to `app/api/`
2. **Business Logic**: Add to `app/services/`
3. **Database Changes**: Create Alembic migration
4. **External Integration**: Add client in `app/clients/`

### Testing
```bash
# Install dev dependencies
uv pip install pytest pytest-asyncio

# Run tests
pytest
```

## Monitoring

### Health Checks
```bash
# Basic health
curl http://localhost:8000/api/v1/health

# Detailed health with scheduler status
curl http://localhost:8000/api/v1/health/detailed
```

### Scheduler Status
```bash
curl http://localhost:8000/api/v1/admin/scheduler/status
```

### Cache Statistics
```bash
curl http://localhost:8000/api/v1/admin/cache/stats
```

## Production Deployment

### Security Considerations
- Change default SECRET_KEY
- Use environment variables for sensitive data
- Configure CORS appropriately
- Set up proper authentication
- Enable SSL/TLS

### Performance Optimization
- Monitor cache hit rates
- Adjust scheduler intervals based on load
- Set up database indexes
- Configure connection pooling

### Monitoring
- Set up structured logging
- Monitor API response times
- Track recommendation success rates
- Monitor external API usage

## License

[Your License Here]

## Contributing

[Contributing Guidelines Here]
