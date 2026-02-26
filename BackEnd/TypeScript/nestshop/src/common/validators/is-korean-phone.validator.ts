import {
  registerDecorator,
  ValidationOptions,
  ValidatorConstraint,
  ValidatorConstraintInterface,
} from 'class-validator';

@ValidatorConstraint({ name: 'isKoreanPhone', async: false })
export class IsKoreanPhoneConstraint implements ValidatorConstraintInterface {
  validate(phone: string): boolean {
    if (!phone) return false;
    // 010-1234-5678, 01012345678, 010 1234 5678 모두 허용
    const cleaned = phone.replace(/[\s-]/g, '');
    return /^01[016789]\d{7,8}$/.test(cleaned);
  }

  defaultMessage(): string {
    return '올바른 한국 휴대전화 번호를 입력해주세요. (예: 010-1234-5678)';
  }
}

export function IsKoreanPhone(validationOptions?: ValidationOptions) {
  return function (object: object, propertyName: string) {
    registerDecorator({
      target: object.constructor,
      propertyName,
      options: validationOptions,
      constraints: [],
      validator: IsKoreanPhoneConstraint,
    });
  };
}
