import { ExecutionContext, HttpStatus } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { JwtAuthGuard } from '../../src/common/guards/jwt-auth.guard';
import { RolesGuard } from '../../src/common/guards/roles.guard';
import { BusinessException } from '../../src/common/exceptions/business.exception';
import { UploadSecurityService } from '../../src/upload/upload-security.service';

describe('Security Regression Test', () => {
  describe('RolesGuard', () => {
    const createContext = (user?: { role?: string }): ExecutionContext =>
      ({
        getHandler: () => ({}),
        getClass: () => ({}),
        switchToHttp: () => ({
          getRequest: () => ({ user }),
        }),
      }) as ExecutionContext;

    it('should allow when no required roles are defined', () => {
      const reflector = {
        getAllAndOverride: jest.fn().mockReturnValue(undefined),
      } as unknown as Reflector;
      const guard = new RolesGuard(reflector);

      expect(guard.canActivate(createContext({ role: 'user' }))).toBe(true);
    });

    it('should throw forbidden when user role is not matched', () => {
      const reflector = {
        getAllAndOverride: jest.fn().mockReturnValue(['admin']),
      } as unknown as Reflector;
      const guard = new RolesGuard(reflector);

      expect(() => guard.canActivate(createContext({ role: 'user' }))).toThrow(BusinessException);
    });
  });

  describe('JwtAuthGuard', () => {
    it('should return user when handleRequest receives user', () => {
      const reflector = {
        getAllAndOverride: jest.fn().mockReturnValue(false),
      } as unknown as Reflector;
      const guard = new JwtAuthGuard(reflector);
      const user = { sub: 1, role: 'user' };

      expect(guard.handleRequest(null, user)).toBe(user);
    });

    it('should throw AUTH_UNAUTHORIZED when handleRequest receives no user', () => {
      const reflector = {
        getAllAndOverride: jest.fn().mockReturnValue(false),
      } as unknown as Reflector;
      const guard = new JwtAuthGuard(reflector);

      try {
        guard.handleRequest(null, null as never);
        fail('expected to throw');
      } catch (error) {
        expect(error).toBeInstanceOf(BusinessException);
        expect((error as BusinessException).errorCode).toBe('AUTH_009');
        expect((error as BusinessException).getStatus()).toBe(HttpStatus.UNAUTHORIZED);
      }
    });
  });

  describe('UploadSecurityService', () => {
    const createService = (extensions: string[] = ['jpg']) => {
      const repository = {
        findOne: jest.fn().mockResolvedValue({
          settingValue: {
            extensions,
          },
        }),
      };
      const cacheService = {
        getJson: jest.fn().mockResolvedValue({ extensions }),
        setJson: jest.fn().mockResolvedValue(undefined),
      };

      const service = new UploadSecurityService(repository as never, cacheService as never);
      return { service };
    };

    it('should validate file when extension and signature match', async () => {
      const { service } = createService(['jpg']);
      const file = {
        originalname: 'sample.jpg',
        buffer: Buffer.from('ffd8ffe000104a464946000101010060', 'hex'),
      } as Express.Multer.File;

      await expect(service.validateFile(file)).resolves.toBeUndefined();
    });

    it('should reject when extension and signature do not match', async () => {
      const { service } = createService(['png', 'jpg']);
      const file = {
        originalname: 'sample.png',
        buffer: Buffer.from('ffd8ffe000104a464946000101010060', 'hex'),
      } as Express.Multer.File;

      await expect(service.validateFile(file)).rejects.toBeInstanceOf(BusinessException);
    });
  });
});
