# PBShop Python Django ORM PostgreSQL

`python-django-pip-djangoorm-postgresql` is the Python reference track for PBShop.

This bootstrap keeps the scope intentionally small:

- Django + pip
- Django ORM + PostgreSQL
- environment-based settings
- minimal `/health`, `/api/v1/`, `/docs/openapi`, and `/docs/swagger` routes
- smoke tests for the bootstrap stage

Domain implementation is intentionally out of scope for this step.

## Quick Start

```powershell
python -m venv .venv
.\.venv\Scripts\python -m pip install -r requirements.txt
Copy-Item .env.example .env
.\.venv\Scripts\python manage.py check
.\.venv\Scripts\python manage.py test
.\.venv\Scripts\python manage.py runserver
```

Docs endpoints after boot:

- OpenAPI JSON: `/docs/openapi`
- Swagger UI: `/docs/swagger`

## CI

GitHub Actions validates the bootstrap track on `push` and `pull_request`.

The first CI scope stays intentionally small:

- `python -m pip install -r requirements.txt`
- `python manage.py check`
- `python manage.py test`
- bootstrap smoke checks for `/health`, `/api/v1/`, `/docs/openapi`, and `/docs/swagger`

Local CI-equivalent commands:

```powershell
Copy-Item .env.example .env
python -m pip install -r requirements.txt
python manage.py check
python manage.py test
```

## Contract References

- `Document/01_requirements.md`
- `Document/02_api-specification.md`
- `Document/03_erd.md`

## Bootstrap Structure

```text
python-django-pip-djangoorm-postgresql
├─ .venv
├─ apps
├─ config
├─ tests
├─ .env.example
├─ .gitignore
├─ manage.py
├─ README.md
└─ requirements.txt
```
