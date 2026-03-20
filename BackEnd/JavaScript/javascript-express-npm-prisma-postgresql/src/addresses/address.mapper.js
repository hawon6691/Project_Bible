export function toAddressDto(item) {
  return {
    id: item.id,
    userId: item.userId,
    label: item.label,
    recipientName: item.recipientName,
    phone: item.phone,
    zipCode: item.zipCode,
    address: item.address,
    addressDetail: item.addressDetail,
    isDefault: item.isDefault,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}
