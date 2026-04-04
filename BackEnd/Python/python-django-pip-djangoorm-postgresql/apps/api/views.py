from apps.api.responses import success_response


def api_root(_request):
    return success_response(
        {
            "service": "pbshop-python-django-pip-djangoorm-postgresql",
            "version": "bootstrap",
            "status": "ok",
        }
    )
