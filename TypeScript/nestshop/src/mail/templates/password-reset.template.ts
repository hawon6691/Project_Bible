export function passwordResetTemplate(code: string, name: string): string {
  return `
<!DOCTYPE html>
<html>
<head><meta charset="utf-8"></head>
<body style="font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
  <div style="background: #f8f9fa; border-radius: 8px; padding: 40px; text-align: center;">
    <h1 style="color: #333; margin-bottom: 10px;">NestShop</h1>
    <h2 style="color: #555; font-weight: normal;">비밀번호 재설정</h2>
    <p style="color: #666; margin: 20px 0;">${name}님, 비밀번호 재설정을 위한 인증코드입니다.</p>
    <div style="background: #fff; border: 2px solid #dc3545; border-radius: 8px; padding: 20px; margin: 30px 0;">
      <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #dc3545;">${code}</span>
    </div>
    <p style="color: #999; font-size: 14px;">이 인증코드는 10분간 유효합니다.</p>
    <p style="color: #999; font-size: 12px; margin-top: 30px;">본인이 요청하지 않은 경우 이 메일을 무시하고 비밀번호를 변경해주세요.</p>
  </div>
</body>
</html>`;
}
