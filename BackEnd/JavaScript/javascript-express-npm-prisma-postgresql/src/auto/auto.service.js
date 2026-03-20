import { badRequest, notFound } from "../utils/http-error.js";
import { findCarModelById, findCarModels, findLeaseOffersByModelId } from "./auto.repository.js";

const CAR_TYPES = new Set(["SEDAN", "SUV", "HATCHBACK", "TRUCK", "VAN", "EV"]);

function toCarModelDto(model) {
  return {
    id: model.id,
    brand: model.brand,
    name: model.name,
    type: model.type,
    year: model.year,
    basePrice: model.basePrice,
    imageUrl: model.imageUrl,
    isActive: model.isActive,
    createdAt: model.createdAt,
    updatedAt: model.updatedAt,
  };
}

function toLeaseOfferDto(offer) {
  return {
    id: offer.id,
    carModelId: offer.carModelId,
    company: offer.company,
    type: offer.type,
    monthlyPayment: offer.monthlyPayment,
    deposit: offer.deposit,
    contractMonths: offer.contractMonths,
    annualMileage: offer.annualMileage,
    isActive: offer.isActive,
    createdAt: offer.createdAt,
    updatedAt: offer.updatedAt,
  };
}

async function requireCarModel(modelId) {
  const model = await findCarModelById(modelId);
  if (!model) {
    throw notFound("Car model not found");
  }
  return model;
}

export async function getCarModels(query) {
  const brand = query?.brand ? String(query.brand).trim() : null;
  const type = query?.type ? String(query.type).trim().toUpperCase() : null;

  if (type && !CAR_TYPES.has(type)) {
    throw badRequest("type must be one of SEDAN, SUV, HATCHBACK, TRUCK, VAN, EV");
  }

  const items = await findCarModels({
    brand: brand || undefined,
    type: type || undefined,
  });

  return items.map(toCarModelDto);
}

export async function getLeaseOffers(modelId) {
  await requireCarModel(modelId);
  const items = await findLeaseOffersByModelId(modelId);
  return items.map(toLeaseOfferDto);
}
