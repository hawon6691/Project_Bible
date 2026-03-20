<?php

namespace App\Common\Exceptions;

use Exception;
use Symfony\Component\HttpFoundation\Response;

class BusinessException extends Exception
{
    public function __construct(
        string $message,
        protected readonly string $errorCode = 'BUSINESS_ERROR',
        protected readonly int $status = Response::HTTP_BAD_REQUEST,
        protected readonly array $context = [],
    ) {
        parent::__construct($message, $status);
    }

    public function errorCode(): string
    {
        return $this->errorCode;
    }

    public function status(): int
    {
        return $this->status;
    }

    public function context(): array
    {
        return $this->context;
    }
}
