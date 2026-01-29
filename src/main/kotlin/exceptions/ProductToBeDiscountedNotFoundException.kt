package com.assignment.exceptions

class ProductToBeDiscountedNotFoundException(productId: String) : Exception("Product with id $productId not found")
