from django.conf import settings
from django.http import JsonResponse


def health_check(_request):
    return JsonResponse(
        {
            "success": True,
            "data": {
                "status": "ok",
                "service": "pbshop-python-django-pip-djangoorm-postgresql",
                "database": settings.DATABASES["default"]["ENGINE"],
            },
        }
    )
