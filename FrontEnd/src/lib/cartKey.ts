const GUEST_CART_KEY = 'nestshop_guest_cart_key';

function makeGuestCartKey() {
  return `guest_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;
}

export function getOrCreateGuestCartKey() {
  const existing = localStorage.getItem(GUEST_CART_KEY);
  if (existing) {
    return existing;
  }

  const generated = makeGuestCartKey();
  localStorage.setItem(GUEST_CART_KEY, generated);
  return generated;
}
