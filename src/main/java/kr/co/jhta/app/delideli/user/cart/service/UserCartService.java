package kr.co.jhta.app.delideli.user.cart.service;

import kr.co.jhta.app.delideli.user.cart.domain.Cart;

import java.util.ArrayList;

public interface UserCartService {

    void addItemToCart(int userKey, int menuKey, int quantity, ArrayList<Integer> selectedOptions);

    ArrayList<Cart> getCartItemsByUser(int userKey);

    Cart getCartItemById(int cartKey);

    void updateCartItem(int cartKey, int quantity, ArrayList<Integer> selectedOptionKeys);

    void deleteCartItem(int cartKey);

    ArrayList<Cart> getCartItemsByStoreInfoKey(int userKey, int storeInfoKey);

    void removeCartItem(int cartKey);

    int addCart(int userKey, int menuKey, int quantity);

    void addCartOption(int cartKey, int optionKey, int optionPrice, String optionName);
}