<?php

namespace App\Modules\Auth\Enums;

final class AuthCodePurpose
{
    public const EMAIL_VERIFICATION = 'EMAIL_VERIFICATION';

    public const PASSWORD_RESET = 'PASSWORD_RESET';

    private function __construct() {}
}
