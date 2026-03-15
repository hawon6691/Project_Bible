import { prisma } from "../prisma.js";

function cartItemSelect() {
  return {
    id: true,
    userId: true,
    productId: true,
    sellerId: true,
    selectedOptions: true,
    quantity: true,
    createdAt: true,
    updatedAt: true,
    product: {
      select: {
        id: true,
        name: true,
        price: true,
        discountPrice: true,
        thumbnailUrl: true,
        status: true,
      },
    },
    seller: {
      select: {
        id: true,
        name: true,
        trustScore: true,
      },
    },
  };
}

export function findCartItems(userId) {
  return prisma.cartItem.findMany({
    where: { userId },
    select: cartItemSelect(),
    orderBy: { id: "asc" },
  });
}

export function createCartItem(data) {
  return prisma.cartItem.create({
    data,
    select: cartItemSelect(),
  });
}

export function updateCartItemQuantity(userId, itemId, quantity) {
  return prisma.cartItem.updateMany({
    where: { id: Number(itemId), userId },
    data: { quantity },
  });
}

export function findCartItemById(itemId) {
  return prisma.cartItem.findUnique({
    where: { id: Number(itemId) },
    select: cartItemSelect(),
  });
}

export function deleteCartItemById(userId, itemId) {
  return prisma.cartItem.deleteMany({
    where: { id: Number(itemId), userId },
  });
}

export function clearCartItems(userId) {
  return prisma.cartItem.deleteMany({ where: { userId } });
}
