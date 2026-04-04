from django.http import HttpResponse, HttpResponseRedirect, JsonResponse

from apps.docs.services import build_openapi_spec, render_swagger_html


def openapi_view(_request):
    return JsonResponse(build_openapi_spec())


def swagger_redirect_view(_request):
    return HttpResponseRedirect("/docs/swagger-ui/index.html")


def swagger_ui_view(_request):
    return HttpResponse(render_swagger_html(), content_type="text/html")
