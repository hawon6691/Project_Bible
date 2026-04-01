from django.http import JsonResponse


def api_root(_request):
    return JsonResponse(
        {
            "success": True,
            "data": {
                "service": "pbshop-python-django-pip-djangoorm-postgresql",
                "version": "bootstrap",
                "status": "ok",
            },
        }
    )
