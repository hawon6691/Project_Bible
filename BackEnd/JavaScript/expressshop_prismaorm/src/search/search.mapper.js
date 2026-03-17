function escapeRegExp(value) {
  return String(value).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

export function highlightText(text, keyword) {
  const source = String(text ?? "");
  const needle = String(keyword ?? "").trim();
  if (!needle) {
    return source;
  }

  const pattern = new RegExp(`(${escapeRegExp(needle)})`, "ig");
  return source.replace(pattern, "<em>$1</em>");
}

export function toSearchHitDto(item) {
  return {
    id: item.id,
    name: item.name,
    lowestPrice: item.lowestPrice,
    thumbnailUrl: item.thumbnailUrl,
    categoryName: item.categoryName,
    score: item.score,
  };
}

export function toAutocompleteProductDto(item) {
  return {
    id: item.id,
    name: item.name,
    thumbnailUrl: item.thumbnailUrl,
    lowestPrice: item.lowestPrice,
  };
}

export function toAutocompleteCategoryDto(item) {
  return {
    id: item.id,
    name: item.name,
  };
}

export function toPopularKeywordDto(item) {
  return {
    rank: item.rank,
    keyword: item.keyword,
    count: item.count,
  };
}

export function toRecentKeywordDto(item) {
  return {
    id: item.id,
    keyword: item.keyword,
    searchedAt: item.searchedAt,
    createdAt: item.createdAt,
  };
}

export function toSearchWeightDto(item) {
  return {
    nameWeight: item.nameWeight,
    keywordWeight: item.keywordWeight,
    clickWeight: item.clickWeight,
  };
}
