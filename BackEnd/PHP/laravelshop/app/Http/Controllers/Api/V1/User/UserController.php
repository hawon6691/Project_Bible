<?php

namespace App\Http\Controllers\Api\V1\User;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\User\Requests\UpdateMeRequest;
use App\Modules\User\Requests\UpdateProfileRequest;
use App\Modules\User\Requests\UpdateUserStatusRequest;
use App\Modules\User\Requests\UploadProfileImageRequest;
use App\Modules\User\Services\UserService;
use Illuminate\Http\Request;

class UserController extends ApiController
{
    public function __construct(
        private readonly UserService $userService,
    ) {
    }

    public function me(Request $request)
    {
        return $this->success($this->userService->getMe($request->user()));
    }

    public function updateMe(UpdateMeRequest $request)
    {
        return $this->success($this->userService->updateMe($request->user(), $request->validated()));
    }

    public function deleteMe(Request $request)
    {
        return $this->success($this->userService->deleteMe($request->user()));
    }

    public function profile(int $id)
    {
        return $this->success($this->userService->getProfile($id));
    }

    public function updateProfile(UpdateProfileRequest $request)
    {
        return $this->success($this->userService->updateProfile($request->user(), $request->validated()));
    }

    public function uploadProfileImage(UploadProfileImageRequest $request)
    {
        return $this->success($this->userService->uploadProfileImage($request->user(), $request->file('image')));
    }

    public function deleteProfileImage(Request $request)
    {
        return $this->success($this->userService->deleteProfileImage($request->user()));
    }

    public function index(Request $request)
    {
        return $this->success($this->userService->listUsers($request->user(), $request->query()));
    }

    public function updateStatus(UpdateUserStatusRequest $request, int $id)
    {
        return $this->success($this->userService->updateUserStatus(
            $request->user(),
            $id,
            $request->validated()['status'],
        ));
    }
}
