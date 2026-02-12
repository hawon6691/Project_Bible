import {
  registerDecorator,
  ValidationArguments,
  ValidationOptions,
  ValidatorConstraint,
  ValidatorConstraintInterface,
} from 'class-validator';

@ValidatorConstraint({ name: 'match', async: false })
export class MatchConstraint implements ValidatorConstraintInterface {
  validate(value: unknown, args: ValidationArguments): boolean {
    const [relatedPropertyName] = args.constraints as string[];
    const relatedValue = (args.object as Record<string, unknown>)[relatedPropertyName];
    return value === relatedValue;
  }

  defaultMessage(args: ValidationArguments): string {
    const [relatedPropertyName] = args.constraints as string[];
    return `${args.property}이(가) ${relatedPropertyName}과(와) 일치하지 않습니다.`;
  }
}

/**
 * 다른 필드와 값이 일치하는지 검증하는 데코레이터
 * @example
 * class ResetPasswordDto {
 *   password: string;
 *   @Match('password')
 *   passwordConfirm: string;
 * }
 */
export function Match(property: string, validationOptions?: ValidationOptions) {
  return function (object: object, propertyName: string) {
    registerDecorator({
      target: object.constructor,
      propertyName,
      options: validationOptions,
      constraints: [property],
      validator: MatchConstraint,
    });
  };
}
