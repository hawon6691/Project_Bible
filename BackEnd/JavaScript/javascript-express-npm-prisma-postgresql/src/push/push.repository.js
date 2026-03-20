import { prisma } from "../prisma.js";

export function findPushSubscriptionByEndpoint(endpoint) {
  return prisma.pushSubscription.findUnique({
    where: { endpoint },
  });
}

export function findActivePushSubscriptionsByUserId(userId) {
  return prisma.pushSubscription.findMany({
    where: {
      userId: Number(userId),
      isActive: true,
    },
    orderBy: [{ createdAt: "desc" }, { id: "desc" }],
  });
}

export function savePushSubscription(userId, data) {
  return prisma.$transaction(async (tx) => {
    const existing = await tx.pushSubscription.findUnique({
      where: { endpoint: data.endpoint },
    });

    if (existing) {
      return tx.pushSubscription.update({
        where: { endpoint: data.endpoint },
        data: {
          userId: Number(userId),
          p256dhKey: data.p256dhKey,
          authKey: data.authKey,
          expirationTime: data.expirationTime,
          isActive: true,
          updatedAt: new Date(),
        },
      });
    }

    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('push_subscriptions', 'id'), COALESCE((SELECT MAX(id) FROM push_subscriptions), 0) + 1, false)",
    );

    return tx.pushSubscription.create({
      data: {
        userId: Number(userId),
        endpoint: data.endpoint,
        p256dhKey: data.p256dhKey,
        authKey: data.authKey,
        expirationTime: data.expirationTime,
        isActive: true,
      },
    });
  });
}

export function deactivatePushSubscription(userId, endpoint) {
  return prisma.pushSubscription.updateMany({
    where: {
      userId: Number(userId),
      endpoint,
    },
    data: {
      isActive: false,
      updatedAt: new Date(),
    },
  });
}

export function findPushPreferenceByUserId(userId) {
  return prisma.pushPreference.findUnique({
    where: { userId: Number(userId) },
  });
}

export function createPushPreference(userId, data) {
  return prisma.$transaction(async (tx) => {
    await tx.$executeRawUnsafe(
      "SELECT setval(pg_get_serial_sequence('push_preferences', 'id'), COALESCE((SELECT MAX(id) FROM push_preferences), 0) + 1, false)",
    );

    return tx.pushPreference.create({
      data: {
        userId: Number(userId),
        ...data,
      },
    });
  });
}

export function updatePushPreference(userId, data) {
  return prisma.pushPreference.update({
    where: { userId: Number(userId) },
    data: {
      ...data,
      updatedAt: new Date(),
    },
  });
}
