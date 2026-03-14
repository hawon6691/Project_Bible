import { Router } from "express";

import { prisma } from "../prisma.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";
import { badRequest, notFound } from "../utils/http-error.js";
import { success } from "../utils/response.js";

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

function orderSummarySelect() {
  return {
    id: true,
    orderNumber: true,
    status: true,
    totalAmount: true,
    pointUsed: true,
    finalAmount: true,
    recipientName: true,
    createdAt: true,
    updatedAt: true,
  };
}

function orderDetailInclude() {
  return {
    orderItems: {
      orderBy: { id: "asc" },
    },
    payments: {
      orderBy: { id: "asc" },
    },
  };
}

export function createCommerceRoutes(apiPrefix) {
  const router = Router();

  router.get(
    `${apiPrefix}/cart`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const items = await prisma.cartItem.findMany({
        where: { userId: req.user.id },
        select: cartItemSelect(),
        orderBy: { id: "asc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.post(
    `${apiPrefix}/cart`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const { productId, sellerId, quantity, selectedOptions } = req.body ?? {};
      if (!productId || !quantity) {
        throw badRequest("productId and quantity are required");
      }

      const created = await prisma.cartItem.create({
        data: {
          userId: req.user.id,
          productId: Number(productId),
          sellerId: sellerId ? Number(sellerId) : null,
          quantity: Number(quantity),
          selectedOptions: selectedOptions ?? null,
        },
        select: cartItemSelect(),
      });

      res.status(201).json(success(created));
    }),
  );

  router.patch(
    `${apiPrefix}/cart/:itemId`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const quantity = Number(req.body?.quantity);
      if (!quantity || quantity < 1) {
        throw badRequest("quantity must be greater than 0");
      }

      const updated = await prisma.cartItem.updateMany({
        where: { id: Number(req.params.itemId), userId: req.user.id },
        data: { quantity },
      });

      if (updated.count === 0) {
        throw notFound("Cart item not found");
      }

      const item = await prisma.cartItem.findUnique({
        where: { id: Number(req.params.itemId) },
        select: cartItemSelect(),
      });

      res.status(200).json(success(item));
    }),
  );

  router.delete(
    `${apiPrefix}/cart/:itemId`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const deleted = await prisma.cartItem.deleteMany({
        where: { id: Number(req.params.itemId), userId: req.user.id },
      });

      if (deleted.count === 0) {
        throw notFound("Cart item not found");
      }

      res.status(200).json(success({ message: "Cart item deleted" }));
    }),
  );

  router.delete(
    `${apiPrefix}/cart`,
    requireAuth,
    asyncHandler(async (req, res) => {
      await prisma.cartItem.deleteMany({
        where: { userId: req.user.id },
      });

      res.status(200).json(success({ message: "Cart cleared" }));
    }),
  );

  router.get(
    `${apiPrefix}/addresses`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const items = await prisma.address.findMany({
        where: { userId: req.user.id },
        select: addressSelect(),
        orderBy: [{ isDefault: "desc" }, { id: "asc" }],
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.post(
    `${apiPrefix}/addresses`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const {
        label,
        recipientName,
        phone,
        zipCode,
        address,
        addressDetail,
        isDefault,
      } = req.body ?? {};

      if (!label || !recipientName || !phone || !zipCode || !address) {
        throw badRequest("label, recipientName, phone, zipCode, address are required");
      }

      if (isDefault) {
        await prisma.address.updateMany({
          where: { userId: req.user.id },
          data: { isDefault: false },
        });
      }

      const created = await prisma.address.create({
        data: {
          userId: req.user.id,
          label,
          recipientName,
          phone,
          zipCode,
          address,
          addressDetail: addressDetail ?? null,
          isDefault: Boolean(isDefault),
        },
        select: addressSelect(),
      });

      res.status(201).json(success(created));
    }),
  );

  router.patch(
    `${apiPrefix}/addresses/:id`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const addressId = Number(req.params.id);
      const existing = await prisma.address.findFirst({
        where: { id: addressId, userId: req.user.id },
      });

      if (!existing) {
        throw notFound("Address not found");
      }

      if (req.body?.isDefault) {
        await prisma.address.updateMany({
          where: { userId: req.user.id },
          data: { isDefault: false },
        });
      }

      const updated = await prisma.address.update({
        where: { id: addressId },
        data: {
          label: req.body?.label ?? undefined,
          recipientName: req.body?.recipientName ?? undefined,
          phone: req.body?.phone ?? undefined,
          zipCode: req.body?.zipCode ?? undefined,
          address: req.body?.address ?? undefined,
          addressDetail: req.body?.addressDetail ?? undefined,
          isDefault:
            typeof req.body?.isDefault === "boolean" ? req.body.isDefault : undefined,
        },
        select: addressSelect(),
      });

      res.status(200).json(success(updated));
    }),
  );

  router.delete(
    `${apiPrefix}/addresses/:id`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const deleted = await prisma.address.deleteMany({
        where: { id: Number(req.params.id), userId: req.user.id },
      });

      if (deleted.count === 0) {
        throw notFound("Address not found");
      }

      res.status(200).json(success({ message: "Address deleted" }));
    }),
  );

  router.get(
    `${apiPrefix}/orders`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const items = await prisma.order.findMany({
        where: { userId: req.user.id },
        select: orderSummarySelect(),
        orderBy: { createdAt: "desc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.get(
    `${apiPrefix}/orders/:id`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const item = await prisma.order.findFirst({
        where: { id: Number(req.params.id), userId: req.user.id },
        include: orderDetailInclude(),
      });

      if (!item) {
        throw notFound("Order not found");
      }

      res.status(200).json(success(item));
    }),
  );

  router.post(
    `${apiPrefix}/orders`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const {
        recipientName,
        recipientPhone,
        zipCode,
        address,
        addressDetail,
        memo,
        pointUsed,
        items,
      } = req.body ?? {};

      if (!recipientName || !recipientPhone || !zipCode || !address || !Array.isArray(items) || items.length === 0) {
        throw badRequest("recipient info and items are required");
      }

      const productIds = items.map((item) => Number(item.productId));
      const products = await prisma.product.findMany({
        where: { id: { in: productIds } },
      });

      const productMap = new Map(products.map((product) => [product.id, product]));
      let totalAmount = 0;

      const orderItems = items.map((item) => {
        const product = productMap.get(Number(item.productId));
        if (!product) {
          throw badRequest(`Product not found: ${item.productId}`);
        }

        const quantity = Number(item.quantity ?? 1);
        const unitPrice = product.discountPrice ?? product.price;
        const totalPrice = unitPrice * quantity;
        totalAmount += totalPrice;

        return {
          productId: product.id,
          sellerId: item.sellerId ? Number(item.sellerId) : null,
          productName: product.name,
          sellerName: item.sellerName ?? null,
          selectedOptions: item.selectedOptions ?? null,
          quantity,
          unitPrice,
          totalPrice,
        };
      });

      const usedPoint = Number(pointUsed ?? 0);
      const finalAmount = Math.max(totalAmount - usedPoint, 0);
      const orderNumber = `ORD-${Date.now()}`;

      const created = await prisma.order.create({
        data: {
          orderNumber,
          userId: req.user.id,
          status: "PENDING",
          totalAmount,
          pointUsed: usedPoint,
          finalAmount,
          recipientName,
          recipientPhone,
          zipCode,
          address,
          addressDetail: addressDetail ?? null,
          memo: memo ?? null,
          orderItems: {
            create: orderItems,
          },
        },
        include: orderDetailInclude(),
      });

      res.status(201).json(success(created));
    }),
  );

  router.post(
    `${apiPrefix}/orders/:id/cancel`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const existing = await prisma.order.findFirst({
        where: { id: Number(req.params.id), userId: req.user.id },
      });

      if (!existing) {
        throw notFound("Order not found");
      }

      const updated = await prisma.order.update({
        where: { id: existing.id },
        data: { status: "CANCELED" },
        include: orderDetailInclude(),
      });

      res.status(200).json(success(updated));
    }),
  );

  router.get(
    `${apiPrefix}/admin/orders`,
    requireAuth,
    requireRole("ADMIN"),
    asyncHandler(async (_req, res) => {
      const items = await prisma.order.findMany({
        select: {
          ...orderSummarySelect(),
          user: {
            select: {
              id: true,
              email: true,
              name: true,
            },
          },
        },
        orderBy: { createdAt: "desc" },
      });

      res.status(200).json(success(items, { total: items.length }));
    }),
  );

  router.patch(
    `${apiPrefix}/admin/orders/:id/status`,
    requireAuth,
    requireRole("ADMIN"),
    asyncHandler(async (req, res) => {
      const status = req.body?.status;
      if (!status) {
        throw badRequest("status is required");
      }

      const updated = await prisma.order.update({
        where: { id: Number(req.params.id) },
        data: { status },
        include: orderDetailInclude(),
      });

      res.status(200).json(success(updated));
    }),
  );

  router.post(
    `${apiPrefix}/payments`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const { orderId, method, amount } = req.body ?? {};
      if (!orderId || !method || !amount) {
        throw badRequest("orderId, method, amount are required");
      }

      const order = await prisma.order.findFirst({
        where: { id: Number(orderId), userId: req.user.id },
      });

      if (!order) {
        throw notFound("Order not found");
      }

      const payment = await prisma.payment.create({
        data: {
          orderId: order.id,
          method,
          amount: Number(amount),
          status: "COMPLETED",
          paidAt: new Date(),
        },
      });

      res.status(201).json(success(payment));
    }),
  );

  router.get(
    `${apiPrefix}/payments/:id`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const payment = await prisma.payment.findFirst({
        where: {
          id: Number(req.params.id),
          order: { userId: req.user.id },
        },
      });

      if (!payment) {
        throw notFound("Payment not found");
      }

      res.status(200).json(success(payment));
    }),
  );

  router.post(
    `${apiPrefix}/payments/:id/refund`,
    requireAuth,
    asyncHandler(async (req, res) => {
      const payment = await prisma.payment.findFirst({
        where: {
          id: Number(req.params.id),
          order: { userId: req.user.id },
        },
      });

      if (!payment) {
        throw notFound("Payment not found");
      }

      const refunded = await prisma.payment.update({
        where: { id: payment.id },
        data: {
          status: "REFUNDED",
          refundedAt: new Date(),
        },
      });

      res.status(200).json(success(refunded));
    }),
  );

  return router;
}
