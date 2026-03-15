import { prisma } from "../prisma.js";

function addressSelect() {
  return {
    id: true,
    userId: true,
    label: true,
    recipientName: true,
    phone: true,
    zipCode: true,
    address: true,
    addressDetail: true,
    isDefault: true,
    createdAt: true,
    updatedAt: true,
  };
}

export function resetDefaultAddresses(userId) {
  return prisma.address.updateMany({ where: { userId }, data: { isDefault: false } });
}

export function findAddresses(userId) {
  return prisma.address.findMany({
    where: { userId },
    select: addressSelect(),
    orderBy: [{ isDefault: "desc" }, { id: "asc" }],
  });
}

export function createAddress(data) {
  return prisma.address.create({ data, select: addressSelect() });
}

export function findAddress(userId, addressId) {
  return prisma.address.findFirst({
    where: { id: Number(addressId), userId },
  });
}

export function updateAddressById(addressId, data) {
  return prisma.address.update({
    where: { id: Number(addressId) },
    data,
    select: addressSelect(),
  });
}

export function deleteAddressById(userId, addressId) {
  return prisma.address.deleteMany({
    where: { id: Number(addressId), userId },
  });
}
