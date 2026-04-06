def _json_content(schema):
    return {
        "content": {
            "application/json": {
                "schema": schema,
            }
        }
    }


def _success_envelope_schema(data_schema=None, include_meta=False):
    schema = {
        "type": "object",
        "required": ["success", "data"],
        "properties": {
            "success": {"type": "boolean", "const": True},
            "data": data_schema or {},
        },
    }
    if include_meta:
        schema["properties"]["meta"] = {"$ref": "#/components/schemas/PaginationMeta"}
    return schema


def _request_body(schema, required=True, content_type="application/json"):
    return {
        "required": required,
        "content": {
            content_type: {
                "schema": schema,
            }
        },
    }


def _auth_requirement(required):
    return [{"bearerAuth": []}] if required else []


def _components():
    return {
        "securitySchemes": {
            "bearerAuth": {
                "type": "http",
                "scheme": "bearer",
                "bearerFormat": "JWT",
            }
        },
        "parameters": {
            "ProviderPath": {
                "name": "provider",
                "in": "path",
                "required": True,
                "schema": {
                    "type": "string",
                    "enum": ["google", "naver", "kakao", "facebook", "instagram"],
                },
            },
            "CodeQuery": {
                "name": "code",
                "in": "query",
                "required": True,
                "schema": {"type": "string"},
            },
            "StateQuery": {
                "name": "state",
                "in": "query",
                "required": True,
                "schema": {"type": "string"},
            },
            "PageQuery": {
                "name": "page",
                "in": "query",
                "required": False,
                "schema": {"type": "integer", "default": 1, "minimum": 1},
            },
            "LimitQuery": {
                "name": "limit",
                "in": "query",
                "required": False,
                "schema": {"type": "integer", "default": 20, "minimum": 1, "maximum": 100},
            },
            "SearchQuery": {
                "name": "search",
                "in": "query",
                "required": False,
                "schema": {"type": "string"},
            },
            "StatusQuery": {
                "name": "status",
                "in": "query",
                "required": False,
                "schema": {"type": "string", "enum": ["ACTIVE", "INACTIVE", "BLOCKED"]},
            },
            "RoleQuery": {
                "name": "role",
                "in": "query",
                "required": False,
                "schema": {"type": "string", "enum": ["USER", "SELLER", "ADMIN"]},
            },
            "UserIdPath": {
                "name": "user_id",
                "in": "path",
                "required": True,
                "schema": {"type": "integer"},
            },
        },
        "schemas": {
            "PaginationMeta": {
                "type": "object",
                "required": ["page", "limit", "totalCount", "totalPages"],
                "properties": {
                    "page": {"type": "integer", "example": 1},
                    "limit": {"type": "integer", "example": 20},
                    "totalCount": {"type": "integer", "example": 2},
                    "totalPages": {"type": "integer", "example": 1},
                },
            },
            "ApiErrorEnvelope": {
                "type": "object",
                "required": ["success", "error"],
                "properties": {
                    "success": {"type": "boolean", "const": False},
                    "error": {
                        "type": "object",
                        "required": ["code", "message"],
                        "properties": {
                            "code": {"type": "string", "example": "VALIDATION_ERROR"},
                            "message": {"type": "string", "example": "입력값 검증 실패"},
                        },
                    },
                },
            },
            "MessageResponse": {
                "type": "object",
                "required": ["message"],
                "properties": {
                    "message": {"type": "string"},
                },
            },
            "HealthResponse": {
                "type": "object",
                "required": ["status", "service", "database"],
                "properties": {
                    "status": {"type": "string", "example": "ok"},
                    "service": {"type": "string", "example": "pbshop-python-django-pip-djangoorm-postgresql"},
                    "database": {"type": "string", "example": "django.db.backends.sqlite3"},
                },
            },
            "ApiRootResponse": {
                "type": "object",
                "required": ["service", "version", "status"],
                "properties": {
                    "service": {"type": "string", "example": "pbshop-python-django-pip-djangoorm-postgresql"},
                    "version": {"type": "string", "example": "bootstrap"},
                    "status": {"type": "string", "example": "ok"},
                },
            },
            "TokenResponse": {
                "type": "object",
                "required": ["accessToken", "refreshToken", "expiresIn"],
                "properties": {
                    "accessToken": {"type": "string"},
                    "refreshToken": {"type": "string"},
                    "expiresIn": {"type": "integer", "example": 1800},
                },
            },
            "AuthSignupRequest": {
                "type": "object",
                "required": ["email", "password", "name", "phone"],
                "properties": {
                    "email": {"type": "string", "format": "email"},
                    "password": {"type": "string"},
                    "name": {"type": "string"},
                    "phone": {"type": "string"},
                },
            },
            "AuthSignupResponse": {
                "type": "object",
                "required": ["id", "email", "name", "message"],
                "properties": {
                    "id": {"type": "integer"},
                    "email": {"type": "string", "format": "email"},
                    "name": {"type": "string"},
                    "message": {"type": "string"},
                },
            },
            "AuthVerifyEmailRequest": {
                "type": "object",
                "required": ["email", "code"],
                "properties": {
                    "email": {"type": "string", "format": "email"},
                    "code": {"type": "string"},
                },
            },
            "AuthVerifyEmailResponse": {
                "type": "object",
                "required": ["message", "verified"],
                "properties": {
                    "message": {"type": "string"},
                    "verified": {"type": "boolean", "example": True},
                },
            },
            "AuthResendVerificationRequest": {
                "type": "object",
                "required": ["email"],
                "properties": {
                    "email": {"type": "string", "format": "email"},
                },
            },
            "AuthLoginRequest": {
                "type": "object",
                "required": ["email", "password"],
                "properties": {
                    "email": {"type": "string", "format": "email"},
                    "password": {"type": "string"},
                },
            },
            "AuthRefreshRequest": {
                "type": "object",
                "required": ["refreshToken"],
                "properties": {
                    "refreshToken": {"type": "string"},
                },
            },
            "AuthPasswordResetRequest": {
                "type": "object",
                "required": ["email", "phone"],
                "properties": {
                    "email": {"type": "string", "format": "email"},
                    "phone": {"type": "string"},
                },
            },
            "AuthVerifyResetCodeRequest": {
                "type": "object",
                "required": ["email", "code"],
                "properties": {
                    "email": {"type": "string", "format": "email"},
                    "code": {"type": "string"},
                },
            },
            "AuthVerifyResetCodeResponse": {
                "type": "object",
                "required": ["resetToken"],
                "properties": {
                    "resetToken": {"type": "string"},
                },
            },
            "AuthResetPasswordRequest": {
                "type": "object",
                "required": ["resetToken", "newPassword"],
                "properties": {
                    "resetToken": {"type": "string"},
                    "newPassword": {"type": "string"},
                },
            },
            "AuthSocialCallbackResponse": {
                "type": "object",
                "required": ["isNewUser"],
                "properties": {
                    "isNewUser": {"type": "boolean"},
                    "accessToken": {"type": "string"},
                    "refreshToken": {"type": "string"},
                    "expiresIn": {"type": "integer"},
                },
            },
            "AuthSocialCompleteRequest": {
                "type": "object",
                "required": ["phone", "nickname"],
                "properties": {
                    "phone": {"type": "string"},
                    "nickname": {"type": "string"},
                },
            },
            "AuthSocialLinkRequest": {
                "type": "object",
                "required": ["provider", "socialToken"],
                "properties": {
                    "provider": {"type": "string", "enum": ["google", "naver", "kakao", "facebook", "instagram"]},
                    "socialToken": {"type": "string"},
                },
            },
            "AuthSocialLinkResponse": {
                "type": "object",
                "required": ["message", "linkedProvider"],
                "properties": {
                    "message": {"type": "string"},
                    "linkedProvider": {"type": "string"},
                },
            },
            "UserResponse": {
                "type": "object",
                "required": ["id", "email", "name", "phone", "role", "status", "point", "badges", "createdAt"],
                "properties": {
                    "id": {"type": "integer"},
                    "email": {"type": "string", "format": "email"},
                    "name": {"type": "string"},
                    "phone": {"type": "string"},
                    "role": {"type": "string", "enum": ["USER", "SELLER", "ADMIN"]},
                    "status": {"type": "string", "enum": ["ACTIVE", "INACTIVE", "BLOCKED"]},
                    "point": {"type": "integer"},
                    "badges": {"type": "array", "items": {}},
                    "createdAt": {"type": "string", "format": "date-time"},
                },
            },
            "UpdateUserRequest": {
                "type": "object",
                "properties": {
                    "name": {"type": "string"},
                    "phone": {"type": "string"},
                    "password": {"type": "string"},
                },
            },
            "UpdateUserStatusRequest": {
                "type": "object",
                "required": ["status"],
                "properties": {
                    "status": {"type": "string", "enum": ["ACTIVE", "INACTIVE", "BLOCKED"]},
                },
            },
            "ProfileResponse": {
                "type": "object",
                "required": ["id", "nickname", "bio", "profileImageUrl"],
                "properties": {
                    "id": {"type": "integer"},
                    "nickname": {"type": "string"},
                    "bio": {"type": ["string", "null"]},
                    "profileImageUrl": {"type": "string"},
                },
            },
            "UpdateProfileRequest": {
                "type": "object",
                "properties": {
                    "nickname": {"type": "string"},
                    "bio": {"type": ["string", "null"]},
                },
            },
            "ProfileImageResponse": {
                "type": "object",
                "required": ["imageUrl"],
                "properties": {
                    "imageUrl": {"type": "string"},
                },
            },
            "CategoryTreeNode": {
                "type": "object",
                "required": ["id", "name", "sortOrder", "children"],
                "properties": {
                    "id": {"type": "integer"},
                    "name": {"type": "string"},
                    "sortOrder": {"type": "integer"},
                    "children": {
                        "type": "array",
                        "items": {"$ref": "#/components/schemas/CategoryTreeNode"},
                    },
                },
            },
            "CategoryResponse": {
                "type": "object",
                "required": ["id", "name", "parentId", "sortOrder", "createdAt"],
                "properties": {
                    "id": {"type": "integer"},
                    "name": {"type": "string"},
                    "parentId": {"type": ["integer", "null"]},
                    "sortOrder": {"type": "integer"},
                    "createdAt": {"type": "string", "format": "date-time"},
                },
            },
            "CategoryDetailResponse": {
                "type": "object",
                "required": ["id", "name", "parentId", "sortOrder", "children", "createdAt"],
                "properties": {
                    "id": {"type": "integer"},
                    "name": {"type": "string"},
                    "parentId": {"type": ["integer", "null"]},
                    "sortOrder": {"type": "integer"},
                    "children": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "required": ["id", "name", "sortOrder"],
                            "properties": {
                                "id": {"type": "integer"},
                                "name": {"type": "string"},
                                "sortOrder": {"type": "integer"},
                            },
                        },
                    },
                    "createdAt": {"type": "string", "format": "date-time"},
                },
            },
            "CreateCategoryRequest": {
                "type": "object",
                "required": ["name"],
                "properties": {
                    "name": {"type": "string"},
                    "parentId": {"type": "integer"},
                    "sortOrder": {"type": "integer", "default": 0},
                },
            },
            "UpdateCategoryRequest": {
                "type": "object",
                "properties": {
                    "name": {"type": "string"},
                    "sortOrder": {"type": "integer"},
                },
            },
            "SpecDefinition": {
                "type": "object",
                "required": ["id", "categoryId", "name", "type", "options", "unit", "isComparable", "dataType", "sortOrder"],
                "properties": {
                    "id": {"type": "integer"},
                    "categoryId": {"type": "integer"},
                    "name": {"type": "string"},
                    "type": {"type": "string", "enum": ["TEXT", "NUMBER", "SELECT"]},
                    "options": {"type": ["array", "null"], "items": {"type": "string"}},
                    "unit": {"type": ["string", "null"]},
                    "isComparable": {"type": "boolean"},
                    "dataType": {"type": "string", "enum": ["NUMBER", "STRING", "BOOLEAN"]},
                    "sortOrder": {"type": "integer"},
                },
            },
            "CreateSpecDefinitionRequest": {
                "type": "object",
                "required": ["categoryId", "name", "type"],
                "properties": {
                    "categoryId": {"type": "integer"},
                    "name": {"type": "string"},
                    "type": {"type": "string", "enum": ["TEXT", "NUMBER", "SELECT"]},
                    "options": {"type": "array", "items": {"type": "string"}},
                    "unit": {"type": ["string", "null"]},
                    "isComparable": {"type": "boolean", "default": True},
                    "dataType": {"type": "string", "enum": ["NUMBER", "STRING", "BOOLEAN"]},
                    "sortOrder": {"type": "integer", "default": 0},
                },
            },
            "UpdateSpecDefinitionRequest": {
                "type": "object",
                "properties": {
                    "categoryId": {"type": "integer"},
                    "name": {"type": "string"},
                    "type": {"type": "string", "enum": ["TEXT", "NUMBER", "SELECT"]},
                    "options": {"type": "array", "items": {"type": "string"}},
                    "unit": {"type": ["string", "null"]},
                    "isComparable": {"type": "boolean"},
                    "dataType": {"type": "string", "enum": ["NUMBER", "STRING", "BOOLEAN"]},
                    "sortOrder": {"type": "integer"},
                },
            },
            "ProductSpecResponse": {
                "type": "object",
                "required": ["id", "specDefinitionId", "name", "value", "numericValue", "unit"],
                "properties": {
                    "id": {"type": "integer"},
                    "specDefinitionId": {"type": "integer"},
                    "name": {"type": "string"},
                    "value": {"type": "string"},
                    "numericValue": {"type": ["number", "null"]},
                    "unit": {"type": ["string", "null"]},
                },
            },
            "SetProductSpecItem": {
                "type": "object",
                "required": ["specDefinitionId", "value"],
                "properties": {
                    "specDefinitionId": {"type": "integer"},
                    "value": {"type": "string"},
                    "numericValue": {"type": ["number", "null"]},
                },
            },
            "SetProductSpecsRequest": {
                "type": "array",
                "items": {"$ref": "#/components/schemas/SetProductSpecItem"},
            },
            "CompareSpecsRequest": {
                "type": "object",
                "required": ["productIds"],
                "properties": {
                    "productIds": {
                        "type": "array",
                        "minItems": 2,
                        "maxItems": 4,
                        "items": {"type": "integer"},
                    }
                },
            },
            "CompareSpecsResponse": {
                "type": "object",
                "required": ["products", "specs"],
                "properties": {
                    "products": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "required": ["id", "name", "thumbnailUrl", "lowestPrice"],
                            "properties": {
                                "id": {"type": "integer"},
                                "name": {"type": "string"},
                                "thumbnailUrl": {"type": ["string", "null"]},
                                "lowestPrice": {"type": "integer"},
                            },
                        },
                    },
                    "specs": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "required": ["name", "values"],
                            "properties": {
                                "name": {"type": "string"},
                                "values": {"type": "array", "items": {"type": "string"}},
                            },
                        },
                    },
                },
            },
            "Seller": {
                "type": "object",
                "required": [
                    "id",
                    "name",
                    "url",
                    "logoUrl",
                    "trustScore",
                    "trustGrade",
                    "description",
                    "isActive",
                    "createdAt",
                ],
                "properties": {
                    "id": {"type": "integer"},
                    "name": {"type": "string"},
                    "url": {"type": "string"},
                    "logoUrl": {"type": ["string", "null"]},
                    "trustScore": {"type": "integer"},
                    "trustGrade": {"type": ["string", "null"]},
                    "description": {"type": ["string", "null"]},
                    "isActive": {"type": "boolean"},
                    "createdAt": {"type": "string", "format": "date-time"},
                },
            },
            "CreateSellerRequest": {
                "type": "object",
                "required": ["name", "url"],
                "properties": {
                    "name": {"type": "string"},
                    "url": {"type": "string"},
                    "logoUrl": {"type": ["string", "null"]},
                    "description": {"type": ["string", "null"]},
                    "trustScore": {"type": "integer", "minimum": 0, "maximum": 100},
                    "trustGrade": {"type": ["string", "null"]},
                    "isActive": {"type": "boolean"},
                },
            },
            "UpdateSellerRequest": {
                "type": "object",
                "properties": {
                    "name": {"type": "string"},
                    "url": {"type": "string"},
                    "logoUrl": {"type": ["string", "null"]},
                    "description": {"type": ["string", "null"]},
                    "trustScore": {"type": "integer", "minimum": 0, "maximum": 100},
                    "trustGrade": {"type": ["string", "null"]},
                    "isActive": {"type": "boolean"},
                },
            },
            "PriceEntry": {
                "type": "object",
                "required": ["id", "seller", "price", "shippingCost", "shippingInfo", "productUrl", "updatedAt"],
                "properties": {
                    "id": {"type": "integer"},
                    "seller": {
                        "type": "object",
                        "required": ["id", "name", "logoUrl", "trustScore"],
                        "properties": {
                            "id": {"type": "integer"},
                            "name": {"type": "string"},
                            "logoUrl": {"type": ["string", "null"]},
                            "trustScore": {"type": "integer"},
                        },
                    },
                    "price": {"type": "integer"},
                    "shippingCost": {"type": "integer"},
                    "shippingInfo": {"type": "string"},
                    "productUrl": {"type": "string"},
                    "updatedAt": {"type": "string", "format": "date-time"},
                },
            },
            "CreatePriceEntryRequest": {
                "type": "object",
                "required": ["sellerId", "price", "productUrl"],
                "properties": {
                    "sellerId": {"type": "integer"},
                    "price": {"type": "integer", "minimum": 0},
                    "shippingCost": {"type": "integer", "minimum": 0},
                    "shippingInfo": {"type": ["string", "null"]},
                    "productUrl": {"type": "string"},
                    "shippingType": {"type": "string", "enum": ["FREE", "PAID", "CONDITIONAL"]},
                },
            },
            "UpdatePriceEntryRequest": {
                "type": "object",
                "properties": {
                    "price": {"type": "integer", "minimum": 0},
                    "shippingCost": {"type": "integer", "minimum": 0},
                    "shippingInfo": {"type": ["string", "null"]},
                    "productUrl": {"type": "string"},
                    "shippingType": {"type": "string", "enum": ["FREE", "PAID", "CONDITIONAL"]},
                    "isAvailable": {"type": "boolean"},
                },
            },
            "ProductPricesResponse": {
                "type": "object",
                "required": ["lowestPrice", "averagePrice", "highestPrice", "entries"],
                "properties": {
                    "lowestPrice": {"type": ["integer", "null"]},
                    "averagePrice": {"type": ["integer", "null"]},
                    "highestPrice": {"type": ["integer", "null"]},
                    "entries": {"type": "array", "items": {"$ref": "#/components/schemas/PriceEntry"}},
                },
            },
            "ProductSummary": {
                "type": "object",
                "required": [
                    "id",
                    "name",
                    "lowestPrice",
                    "sellerCount",
                    "thumbnailUrl",
                    "reviewCount",
                    "averageRating",
                    "priceDiff",
                    "priceDiffPercent",
                    "createdAt",
                ],
                "properties": {
                    "id": {"type": "integer"},
                    "name": {"type": "string"},
                    "lowestPrice": {"type": "integer"},
                    "sellerCount": {"type": "integer"},
                    "thumbnailUrl": {"type": ["string", "null"]},
                    "reviewCount": {"type": "integer"},
                    "averageRating": {"type": "number"},
                    "priceDiff": {"type": ["integer", "null"]},
                    "priceDiffPercent": {"type": ["number", "null"]},
                    "createdAt": {"type": "string", "format": "date-time"},
                },
            },
            "ProductOption": {
                "type": "object",
                "required": ["id", "name", "values"],
                "properties": {
                    "id": {"type": "integer"},
                    "name": {"type": "string"},
                    "values": {"type": "array", "items": {"type": "string"}},
                },
            },
            "ProductImage": {
                "type": "object",
                "required": ["id", "url", "isMain", "sortOrder"],
                "properties": {
                    "id": {"type": "integer"},
                    "url": {"type": "string"},
                    "isMain": {"type": "boolean"},
                    "sortOrder": {"type": "integer"},
                },
            },
            "CreateProductOptionRequest": {
                "type": "object",
                "required": ["name", "values"],
                "properties": {
                    "name": {"type": "string"},
                    "values": {"type": "array", "items": {"type": "string"}},
                },
            },
            "CreateProductImageRequest": {
                "type": "object",
                "required": ["url"],
                "properties": {
                    "url": {"type": "string"},
                    "isMain": {"type": "boolean"},
                    "sortOrder": {"type": "integer"},
                },
            },
            "CreateProductRequest": {
                "type": "object",
                "required": ["name", "description", "price", "stock", "categoryId"],
                "properties": {
                    "name": {"type": "string"},
                    "description": {"type": "string"},
                    "price": {"type": "integer"},
                    "discountPrice": {"type": ["integer", "null"]},
                    "stock": {"type": "integer"},
                    "status": {"type": "string", "enum": ["ON_SALE", "SOLD_OUT", "HIDDEN"]},
                    "categoryId": {"type": "integer"},
                    "thumbnailUrl": {"type": ["string", "null"]},
                    "options": {
                        "type": "array",
                        "items": {"$ref": "#/components/schemas/CreateProductOptionRequest"},
                    },
                    "images": {
                        "type": "array",
                        "items": {"$ref": "#/components/schemas/CreateProductImageRequest"},
                    },
                },
            },
            "UpdateProductRequest": {
                "type": "object",
                "properties": {
                    "name": {"type": "string"},
                    "description": {"type": "string"},
                    "price": {"type": "integer"},
                    "discountPrice": {"type": ["integer", "null"]},
                    "stock": {"type": "integer"},
                    "status": {"type": "string", "enum": ["ON_SALE", "SOLD_OUT", "HIDDEN"]},
                    "categoryId": {"type": "integer"},
                    "thumbnailUrl": {"type": ["string", "null"]},
                },
            },
            "ProductDetail": {
                "type": "object",
                "required": [
                    "id",
                    "name",
                    "description",
                    "lowestPrice",
                    "highestPrice",
                    "averagePrice",
                    "stock",
                    "status",
                    "category",
                    "options",
                    "images",
                    "specs",
                    "priceEntries",
                    "reviewCount",
                    "averageRating",
                    "createdAt",
                ],
                "properties": {
                    "id": {"type": "integer"},
                    "name": {"type": "string"},
                    "description": {"type": "string"},
                    "lowestPrice": {"type": "integer"},
                    "highestPrice": {"type": ["integer", "null"]},
                    "averagePrice": {"type": ["integer", "null"]},
                    "stock": {"type": "integer"},
                    "status": {"type": "string", "enum": ["ON_SALE", "SOLD_OUT", "HIDDEN"]},
                    "category": {
                        "type": "object",
                        "required": ["id", "name"],
                        "properties": {
                            "id": {"type": "integer"},
                            "name": {"type": "string"},
                        },
                    },
                    "options": {"type": "array", "items": {"$ref": "#/components/schemas/ProductOption"}},
                    "images": {"type": "array", "items": {"$ref": "#/components/schemas/ProductImage"}},
                    "specs": {"type": "array", "items": {"$ref": "#/components/schemas/ProductSpecResponse"}},
                    "priceEntries": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "required": ["seller", "price", "url", "shipping"],
                            "properties": {
                                "seller": {
                                    "type": "object",
                                    "required": ["id", "name", "logoUrl", "trustScore"],
                                    "properties": {
                                        "id": {"type": "integer"},
                                        "name": {"type": "string"},
                                        "logoUrl": {"type": ["string", "null"]},
                                        "trustScore": {"type": "integer"},
                                    },
                                },
                                "price": {"type": "integer"},
                                "url": {"type": "string"},
                                "shipping": {"type": "string"},
                            },
                        },
                    },
                    "reviewCount": {"type": "integer"},
                    "averageRating": {"type": "number"},
                    "createdAt": {"type": "string", "format": "date-time"},
                },
            },
        },
    }


def build_openapi_spec():
    token_envelope = _success_envelope_schema({"$ref": "#/components/schemas/TokenResponse"})
    message_envelope = _success_envelope_schema({"$ref": "#/components/schemas/MessageResponse"})
    paths = {
        "/health": {
            "get": {
                "tags": ["Health"],
                "summary": "Health check",
                "responses": {
                    "200": {
                        "description": "Application health state",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/HealthResponse"})),
                    }
                },
                "security": [],
            }
        },
        "/api/v1/": {
            "get": {
                "tags": ["System"],
                "summary": "API root",
                "responses": {
                    "200": {
                        "description": "Bootstrap API metadata",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ApiRootResponse"})),
                    }
                },
                "security": [],
            }
        },
        "/api/v1/auth/signup": {
            "post": {
                "tags": ["Auth"],
                "summary": "Sign up",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthSignupRequest"}),
                "responses": {
                    "201": {
                        "description": "Signed up and verification email sent",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/AuthSignupResponse"})),
                    },
                    "409": {
                        "description": "Duplicate email",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/auth/verify-email": {
            "post": {
                "tags": ["Auth"],
                "summary": "Verify signup email",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthVerifyEmailRequest"}),
                "responses": {
                    "200": {
                        "description": "Email verified",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/AuthVerifyEmailResponse"})),
                    },
                    "400": {
                        "description": "Invalid verification code",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "410": {
                        "description": "Expired verification code",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "429": {
                        "description": "Verification attempt exceeded",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/auth/resend-verification": {
            "post": {
                "tags": ["Auth"],
                "summary": "Resend signup verification code",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthResendVerificationRequest"}),
                "responses": {
                    "200": {
                        "description": "Verification code sent",
                        **_json_content(message_envelope),
                    },
                    "429": {
                        "description": "Too many resend attempts",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/auth/login": {
            "post": {
                "tags": ["Auth"],
                "summary": "Login with email and password",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthLoginRequest"}),
                "responses": {
                    "200": {
                        "description": "Login success",
                        **_json_content(token_envelope),
                    },
                    "403": {
                        "description": "Email not verified or user blocked",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/auth/logout": {
            "post": {
                "tags": ["Auth"],
                "summary": "Logout current user",
                "responses": {
                    "200": {
                        "description": "Logout success",
                        **_json_content(message_envelope),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            }
        },
        "/api/v1/auth/refresh": {
            "post": {
                "tags": ["Auth"],
                "summary": "Refresh access token",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthRefreshRequest"}),
                "responses": {
                    "200": {
                        "description": "Refresh success",
                        **_json_content(token_envelope),
                    },
                    "401": {
                        "description": "Invalid refresh token",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/auth/password-reset/request": {
            "post": {
                "tags": ["Auth"],
                "summary": "Request password reset verification code",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthPasswordResetRequest"}),
                "responses": {
                    "200": {
                        "description": "Password reset verification sent",
                        **_json_content(message_envelope),
                    }
                },
                "security": [],
            }
        },
        "/api/v1/auth/password-reset/verify": {
            "post": {
                "tags": ["Auth"],
                "summary": "Verify password reset code",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthVerifyResetCodeRequest"}),
                "responses": {
                    "200": {
                        "description": "Reset code verified",
                        **_json_content(
                            _success_envelope_schema({"$ref": "#/components/schemas/AuthVerifyResetCodeResponse"})
                        ),
                    },
                    "400": {
                        "description": "Invalid reset code",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/auth/password-reset/confirm": {
            "post": {
                "tags": ["Auth"],
                "summary": "Confirm password reset",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthResetPasswordRequest"}),
                "responses": {
                    "200": {
                        "description": "Password changed",
                        **_json_content(message_envelope),
                    },
                    "400": {
                        "description": "Invalid password reset request",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/auth/login/{provider}": {
            "get": {
                "tags": ["Auth"],
                "summary": "Start social login",
                "parameters": [{"$ref": "#/components/parameters/ProviderPath"}],
                "responses": {
                    "302": {
                        "description": "Redirect to social login provider",
                    }
                },
                "security": [],
            }
        },
        "/api/v1/auth/callback/{provider}": {
            "get": {
                "tags": ["Auth"],
                "summary": "Complete social login callback",
                "parameters": [
                    {"$ref": "#/components/parameters/ProviderPath"},
                    {"$ref": "#/components/parameters/CodeQuery"},
                    {"$ref": "#/components/parameters/StateQuery"},
                ],
                "responses": {
                    "200": {
                        "description": "Social login callback handled",
                        **_json_content(
                            _success_envelope_schema({"$ref": "#/components/schemas/AuthSocialCallbackResponse"})
                        ),
                    },
                    "400": {
                        "description": "Invalid social callback request",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/auth/social/complete": {
            "post": {
                "tags": ["Auth"],
                "summary": "Complete social signup",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthSocialCompleteRequest"}),
                "responses": {
                    "200": {
                        "description": "Social signup completed",
                        **_json_content(token_envelope),
                    },
                    "400": {
                        "description": "Invalid pending social signup",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/auth/social/link": {
            "post": {
                "tags": ["Auth"],
                "summary": "Link social account to current user",
                "requestBody": _request_body({"$ref": "#/components/schemas/AuthSocialLinkRequest"}),
                "responses": {
                    "200": {
                        "description": "Social account linked",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/AuthSocialLinkResponse"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            }
        },
        "/api/v1/auth/social/unlink/{provider}": {
            "delete": {
                "tags": ["Auth"],
                "summary": "Unlink social account from current user",
                "parameters": [{"$ref": "#/components/parameters/ProviderPath"}],
                "responses": {
                    "200": {
                        "description": "Social account unlinked",
                        **_json_content(message_envelope),
                    },
                    "400": {
                        "description": "Cannot unlink last social account",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            }
        },
        "/api/v1/products": {
            "get": {
                "tags": ["Products"],
                "summary": "List products",
                "parameters": [
                    {"$ref": "#/components/parameters/PageQuery"},
                    {"$ref": "#/components/parameters/LimitQuery"},
                    {
                        "name": "categoryId",
                        "in": "query",
                        "required": False,
                        "schema": {"type": "integer"},
                    },
                    {"$ref": "#/components/parameters/SearchQuery"},
                    {
                        "name": "minPrice",
                        "in": "query",
                        "required": False,
                        "schema": {"type": "integer", "minimum": 0},
                    },
                    {
                        "name": "maxPrice",
                        "in": "query",
                        "required": False,
                        "schema": {"type": "integer", "minimum": 0},
                    },
                    {
                        "name": "sort",
                        "in": "query",
                        "required": False,
                        "schema": {
                            "type": "string",
                            "default": "newest",
                            "enum": ["newest", "popularity", "price_asc", "price_desc", "rating_desc", "rating_asc"],
                        },
                    },
                    {
                        "name": "specs",
                        "in": "query",
                        "required": False,
                        "schema": {"type": "string"},
                    },
                ],
                "responses": {
                    "200": {
                        "description": "Paginated product list",
                        **_json_content(
                            _success_envelope_schema(
                                {"type": "array", "items": {"$ref": "#/components/schemas/ProductSummary"}},
                                include_meta=True,
                            )
                        ),
                    },
                    "400": {
                        "description": "Invalid query parameters",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            },
            "post": {
                "tags": ["Products"],
                "summary": "Create product",
                "requestBody": _request_body({"$ref": "#/components/schemas/CreateProductRequest"}),
                "responses": {
                    "201": {
                        "description": "Product created",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProductDetail"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Category not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/products/{product_id}": {
            "get": {
                "tags": ["Products"],
                "summary": "Get product detail",
                "parameters": [
                    {
                        "name": "product_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Product detail",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProductDetail"})),
                    },
                    "404": {
                        "description": "Product not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            },
            "patch": {
                "tags": ["Products"],
                "summary": "Update product",
                "parameters": [
                    {
                        "name": "product_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "requestBody": _request_body({"$ref": "#/components/schemas/UpdateProductRequest"}),
                "responses": {
                    "200": {
                        "description": "Product updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProductDetail"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
            "delete": {
                "tags": ["Products"],
                "summary": "Delete product",
                "parameters": [
                    {
                        "name": "product_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Product deleted",
                        **_json_content(message_envelope),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/products/{product_id}/options": {
            "post": {
                "tags": ["Products"],
                "summary": "Create product option",
                "parameters": [
                    {
                        "name": "product_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "requestBody": _request_body({"$ref": "#/components/schemas/CreateProductOptionRequest"}),
                "responses": {
                    "201": {
                        "description": "Product option created",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProductOption"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            }
        },
        "/api/v1/products/{product_id}/options/{option_id}": {
            "patch": {
                "tags": ["Products"],
                "summary": "Update product option",
                "parameters": [
                    {"name": "product_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                    {"name": "option_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "requestBody": _request_body({"$ref": "#/components/schemas/CreateProductOptionRequest"}),
                "responses": {
                    "200": {
                        "description": "Product option updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProductOption"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product option not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
            "delete": {
                "tags": ["Products"],
                "summary": "Delete product option",
                "parameters": [
                    {"name": "product_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                    {"name": "option_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "responses": {
                    "200": {
                        "description": "Product option deleted",
                        **_json_content(message_envelope),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product option not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/products/{product_id}/images": {
            "post": {
                "tags": ["Products"],
                "summary": "Upload product image",
                "parameters": [
                    {"name": "product_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "requestBody": _request_body(
                    {
                        "type": "object",
                        "required": ["image"],
                        "properties": {
                            "image": {"type": "string", "format": "binary"},
                            "isMain": {"type": "boolean"},
                            "sortOrder": {"type": "integer"},
                        },
                    },
                    content_type="multipart/form-data",
                ),
                "responses": {
                    "201": {
                        "description": "Product image uploaded",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProductImage"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            }
        },
        "/api/v1/products/{product_id}/images/{image_id}": {
            "delete": {
                "tags": ["Products"],
                "summary": "Delete product image",
                "parameters": [
                    {"name": "product_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                    {"name": "image_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "responses": {
                    "200": {
                        "description": "Product image deleted",
                        **_json_content(message_envelope),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product image not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            }
        },
        "/api/v1/products/{product_id}/specs": {
            "get": {
                "tags": ["Specs"],
                "summary": "List product specs",
                "parameters": [
                    {
                        "name": "product_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Product spec list",
                        **_json_content(
                            _success_envelope_schema(
                                {"type": "array", "items": {"$ref": "#/components/schemas/ProductSpecResponse"}}
                            )
                        ),
                    },
                    "404": {
                        "description": "Product not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            },
            "put": {
                "tags": ["Specs"],
                "summary": "Replace product specs",
                "parameters": [
                    {
                        "name": "product_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "requestBody": _request_body({"$ref": "#/components/schemas/SetProductSpecsRequest"}),
                "responses": {
                    "200": {
                        "description": "Product specs replaced",
                        **_json_content(
                            _success_envelope_schema(
                                {"type": "array", "items": {"$ref": "#/components/schemas/ProductSpecResponse"}}
                            )
                        ),
                    },
                    "400": {
                        "description": "Invalid product spec payload",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product or spec definition not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/specs/definitions": {
            "get": {
                "tags": ["Specs"],
                "summary": "List spec definitions",
                "parameters": [
                    {
                        "name": "categoryId",
                        "in": "query",
                        "required": False,
                        "schema": {"type": "integer"},
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Spec definition list",
                        **_json_content(
                            _success_envelope_schema(
                                {"type": "array", "items": {"$ref": "#/components/schemas/SpecDefinition"}}
                            )
                        ),
                    }
                },
                "security": [],
            },
            "post": {
                "tags": ["Specs"],
                "summary": "Create spec definition",
                "requestBody": _request_body({"$ref": "#/components/schemas/CreateSpecDefinitionRequest"}),
                "responses": {
                    "201": {
                        "description": "Spec definition created",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/SpecDefinition"})),
                    },
                    "400": {
                        "description": "Invalid spec definition payload",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Category not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/specs/definitions/{definition_id}": {
            "patch": {
                "tags": ["Specs"],
                "summary": "Update spec definition",
                "parameters": [
                    {
                        "name": "definition_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "requestBody": _request_body({"$ref": "#/components/schemas/UpdateSpecDefinitionRequest"}),
                "responses": {
                    "200": {
                        "description": "Spec definition updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/SpecDefinition"})),
                    },
                    "400": {
                        "description": "Invalid spec definition payload",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Spec definition not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
            "delete": {
                "tags": ["Specs"],
                "summary": "Delete spec definition",
                "parameters": [
                    {
                        "name": "definition_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Spec definition deleted",
                        **_json_content(message_envelope),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Spec definition not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "409": {
                        "description": "Spec definition is in use",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/specs/compare": {
            "post": {
                "tags": ["Specs"],
                "summary": "Compare products by specs",
                "requestBody": _request_body({"$ref": "#/components/schemas/CompareSpecsRequest"}),
                "responses": {
                    "200": {
                        "description": "Basic product spec comparison",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/CompareSpecsResponse"})),
                    },
                    "400": {
                        "description": "Invalid compare request",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            },
        },
        "/api/v1/products/{product_id}/prices": {
            "get": {
                "tags": ["Prices"],
                "summary": "List seller prices for product",
                "parameters": [
                    {"name": "product_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "responses": {
                    "200": {
                        "description": "Product price comparison",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProductPricesResponse"})),
                    },
                    "404": {
                        "description": "Product not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            },
            "post": {
                "tags": ["Prices"],
                "summary": "Create product seller price",
                "parameters": [
                    {"name": "product_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "requestBody": _request_body({"$ref": "#/components/schemas/CreatePriceEntryRequest"}),
                "responses": {
                    "201": {
                        "description": "Price entry created",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/PriceEntry"})),
                    },
                    "400": {
                        "description": "Invalid price payload",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Seller or admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Product or seller not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "409": {
                        "description": "Duplicate price entry",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/prices/{price_id}": {
            "patch": {
                "tags": ["Prices"],
                "summary": "Update price entry",
                "parameters": [
                    {"name": "price_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "requestBody": _request_body({"$ref": "#/components/schemas/UpdatePriceEntryRequest"}),
                "responses": {
                    "200": {
                        "description": "Price entry updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/PriceEntry"})),
                    },
                    "400": {
                        "description": "Invalid price payload",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Seller or admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Price entry not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
            "delete": {
                "tags": ["Prices"],
                "summary": "Delete price entry",
                "parameters": [
                    {"name": "price_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "responses": {
                    "200": {
                        "description": "Price entry deleted",
                        **_json_content(message_envelope),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Price entry not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/sellers": {
            "get": {
                "tags": ["Sellers"],
                "summary": "List sellers",
                "parameters": [
                    {"$ref": "#/components/parameters/PageQuery"},
                    {"$ref": "#/components/parameters/LimitQuery"},
                ],
                "responses": {
                    "200": {
                        "description": "Paginated seller list",
                        **_json_content(
                            _success_envelope_schema(
                                {"type": "array", "items": {"$ref": "#/components/schemas/Seller"}},
                                include_meta=True,
                            )
                        ),
                    }
                },
                "security": [],
            },
            "post": {
                "tags": ["Sellers"],
                "summary": "Create seller",
                "requestBody": _request_body({"$ref": "#/components/schemas/CreateSellerRequest"}),
                "responses": {
                    "201": {
                        "description": "Seller created",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/Seller"})),
                    },
                    "400": {
                        "description": "Invalid seller payload",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/sellers/{seller_id}": {
            "get": {
                "tags": ["Sellers"],
                "summary": "Get seller detail",
                "parameters": [
                    {"name": "seller_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "responses": {
                    "200": {
                        "description": "Seller detail",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/Seller"})),
                    },
                    "404": {
                        "description": "Seller not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            },
            "patch": {
                "tags": ["Sellers"],
                "summary": "Update seller",
                "parameters": [
                    {"name": "seller_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "requestBody": _request_body({"$ref": "#/components/schemas/UpdateSellerRequest"}),
                "responses": {
                    "200": {
                        "description": "Seller updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/Seller"})),
                    },
                    "400": {
                        "description": "Invalid seller payload",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Seller not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
            "delete": {
                "tags": ["Sellers"],
                "summary": "Deactivate seller",
                "parameters": [
                    {"name": "seller_id", "in": "path", "required": True, "schema": {"type": "integer"}},
                ],
                "responses": {
                    "200": {
                        "description": "Seller deactivated",
                        **_json_content(message_envelope),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Seller not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/categories": {
            "get": {
                "tags": ["Categories"],
                "summary": "List category tree",
                "responses": {
                    "200": {
                        "description": "Category tree",
                        **_json_content(
                            _success_envelope_schema(
                                {"type": "array", "items": {"$ref": "#/components/schemas/CategoryTreeNode"}}
                            )
                        ),
                    }
                },
                "security": [],
            },
            "post": {
                "tags": ["Categories"],
                "summary": "Create category",
                "requestBody": _request_body({"$ref": "#/components/schemas/CreateCategoryRequest"}),
                "responses": {
                    "201": {
                        "description": "Category created",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/CategoryResponse"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Parent category not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/categories/{category_id}": {
            "get": {
                "tags": ["Categories"],
                "summary": "Get category detail",
                "parameters": [
                    {
                        "name": "category_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Category detail",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/CategoryDetailResponse"})),
                    },
                    "404": {
                        "description": "Category not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            },
            "patch": {
                "tags": ["Categories"],
                "summary": "Update category",
                "parameters": [
                    {
                        "name": "category_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "requestBody": _request_body({"$ref": "#/components/schemas/UpdateCategoryRequest"}),
                "responses": {
                    "200": {
                        "description": "Category updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/CategoryResponse"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Category not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
            "delete": {
                "tags": ["Categories"],
                "summary": "Delete category",
                "parameters": [
                    {
                        "name": "category_id",
                        "in": "path",
                        "required": True,
                        "schema": {"type": "integer"},
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Category deleted",
                        **_json_content(message_envelope),
                    },
                    "400": {
                        "description": "Category has children",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                    "404": {
                        "description": "Category not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/users": {
            "get": {
                "tags": ["Users"],
                "summary": "List users",
                "parameters": [
                    {"$ref": "#/components/parameters/PageQuery"},
                    {"$ref": "#/components/parameters/LimitQuery"},
                    {"$ref": "#/components/parameters/SearchQuery"},
                    {"$ref": "#/components/parameters/StatusQuery"},
                    {"$ref": "#/components/parameters/RoleQuery"},
                ],
                "responses": {
                    "200": {
                        "description": "Paginated user list",
                        **_json_content(
                            _success_envelope_schema(
                                {"type": "array", "items": {"$ref": "#/components/schemas/UserResponse"}},
                                include_meta=True,
                            )
                        ),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            }
        },
        "/api/v1/users/me": {
            "get": {
                "tags": ["Users"],
                "summary": "Get current user",
                "responses": {
                    "200": {
                        "description": "Current user detail",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/UserResponse"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
            "patch": {
                "tags": ["Users"],
                "summary": "Update current user",
                "requestBody": _request_body({"$ref": "#/components/schemas/UpdateUserRequest"}),
                "responses": {
                    "200": {
                        "description": "Current user updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/UserResponse"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
            "delete": {
                "tags": ["Users"],
                "summary": "Deactivate current user",
                "responses": {
                    "200": {
                        "description": "Current user deleted logically",
                        **_json_content(message_envelope),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/api/v1/users/{user_id}/status": {
            "patch": {
                "tags": ["Users"],
                "summary": "Update user status",
                "parameters": [{"$ref": "#/components/parameters/UserIdPath"}],
                "requestBody": _request_body({"$ref": "#/components/schemas/UpdateUserStatusRequest"}),
                "responses": {
                    "200": {
                        "description": "User status updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/UserResponse"})),
                    },
                    "403": {
                        "description": "Admin role required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            }
        },
        "/api/v1/users/{user_id}/profile": {
            "get": {
                "tags": ["Users"],
                "summary": "Get public user profile",
                "parameters": [{"$ref": "#/components/parameters/UserIdPath"}],
                "responses": {
                    "200": {
                        "description": "Public profile",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProfileResponse"})),
                    },
                    "404": {
                        "description": "User not found",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": [],
            }
        },
        "/api/v1/users/me/profile": {
            "patch": {
                "tags": ["Users"],
                "summary": "Update current user profile",
                "requestBody": _request_body({"$ref": "#/components/schemas/UpdateProfileRequest"}),
                "responses": {
                    "200": {
                        "description": "Profile updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProfileResponse"})),
                    },
                    "409": {
                        "description": "Nickname conflict",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            }
        },
        "/api/v1/users/me/profile-image": {
            "post": {
                "tags": ["Users"],
                "summary": "Upload profile image",
                "requestBody": _request_body(
                    {
                        "type": "object",
                        "required": ["image"],
                        "properties": {
                            "image": {"type": "string", "format": "binary"},
                        },
                    },
                    content_type="multipart/form-data",
                ),
                "responses": {
                    "200": {
                        "description": "Profile image updated",
                        **_json_content(_success_envelope_schema({"$ref": "#/components/schemas/ProfileImageResponse"})),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
            "delete": {
                "tags": ["Users"],
                "summary": "Reset profile image to default",
                "responses": {
                    "200": {
                        "description": "Profile image reset",
                        **_json_content(message_envelope),
                    },
                    "401": {
                        "description": "Authentication required",
                        **_json_content({"$ref": "#/components/schemas/ApiErrorEnvelope"}),
                    },
                },
                "security": _auth_requirement(True),
            },
        },
        "/docs/openapi": {
            "get": {
                "tags": ["Docs"],
                "summary": "OpenAPI document",
                "responses": {
                    "200": {
                        "description": "OpenAPI JSON document",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "type": "object",
                                    "required": ["openapi", "info", "paths"],
                                    "properties": {
                                        "openapi": {"type": "string"},
                                        "info": {"type": "object"},
                                        "paths": {"type": "object"},
                                    },
                                }
                            }
                        },
                    }
                },
                "security": [],
            }
        },
        "/docs/swagger": {
            "get": {
                "tags": ["Docs"],
                "summary": "Swagger redirect",
                "responses": {
                    "302": {
                        "description": "Redirect to Swagger UI",
                    }
                },
                "security": [],
            }
        },
        "/docs/swagger-ui/index.html": {
            "get": {
                "tags": ["Docs"],
                "summary": "Swagger UI",
                "responses": {
                    "200": {
                        "description": "Swagger UI HTML",
                        "content": {
                            "text/html": {
                                "schema": {"type": "string"},
                            }
                        },
                    }
                },
                "security": [],
            }
        },
    }
    return {
        "openapi": "3.1.0",
        "info": {
            "title": "PBShop Python Django ORM API",
            "version": "1.0.0",
            "description": "Manual OpenAPI catalog for the current Django implementation.",
        },
        "servers": [{"url": "http://localhost:8000"}],
        "tags": [
            {"name": "Health"},
            {"name": "System"},
            {"name": "Auth"},
            {"name": "Products"},
            {"name": "Specs"},
            {"name": "Sellers"},
            {"name": "Prices"},
            {"name": "Categories"},
            {"name": "Users"},
            {"name": "Docs"},
        ],
        "components": _components(),
        "paths": paths,
    }


def render_swagger_html():
    return """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>PBShop Python Django ORM API Docs</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
  <style>
    body { margin: 0; background: #f5f7fb; }
    .topbar { display: none; }
    .fallback {
      font-family: Arial, sans-serif;
      max-width: 960px;
      margin: 0 auto;
      padding: 16px 20px 0;
      color: #1f2937;
    }
  </style>
</head>
<body>
  <div class="fallback">
    <p>If Swagger UI does not load, open <a href="/docs/openapi">/docs/openapi</a>.</p>
  </div>
  <div id="swagger-ui"></div>
  <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
  <script>
    window.onload = function () {
      if (!window.SwaggerUIBundle) {
        return;
      }
      window.SwaggerUIBundle({
        url: "/docs/openapi",
        dom_id: "#swagger-ui",
        presets: [window.SwaggerUIBundle.presets.apis],
        layout: "BaseLayout",
        deepLinking: true,
      });
    };
  </script>
</body>
</html>
"""
