import { prisma } from "../prisma.js";

export function findAdminSettingByKey(settingKey) {
  return prisma.adminSetting.findUnique({
    where: { settingKey: String(settingKey) },
  });
}

export function upsertAdminSetting(settingKey, settingValue, updatedBy, description) {
  return prisma.$transaction(async (tx) => {
    const existing = await tx.adminSetting.findUnique({
      where: { settingKey: String(settingKey) },
    });

    if (!existing) {
      await tx.$executeRawUnsafe(
        "SELECT setval(pg_get_serial_sequence('admin_settings', 'id'), COALESCE((SELECT MAX(id) FROM admin_settings), 0) + 1, false)",
      );

      return tx.adminSetting.create({
        data: {
          settingKey: String(settingKey),
          settingValue,
          description,
          updatedBy: updatedBy ?? null,
        },
      });
    }

    return tx.adminSetting.update({
      where: { id: existing.id },
      data: {
        settingValue,
        description,
        updatedBy: updatedBy ?? null,
        updatedAt: new Date(),
      },
    });
  });
}
