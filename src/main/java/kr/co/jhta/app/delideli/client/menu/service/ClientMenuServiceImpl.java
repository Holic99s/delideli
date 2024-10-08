package kr.co.jhta.app.delideli.client.menu.service;

import kr.co.jhta.app.delideli.client.menu.domain.ClientMenu;
import kr.co.jhta.app.delideli.client.menu.domain.ClientMenuGroup;
import kr.co.jhta.app.delideli.client.menu.domain.ClientOptionGroup;
import kr.co.jhta.app.delideli.client.menu.mapper.ClientMenuMapper;
import kr.co.jhta.app.delideli.client.menu.mapper.ClientOptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ClientMenuServiceImpl implements ClientMenuService {

    @Autowired
    private final ClientMenuMapper clientMenuMapper;
    @Autowired
    private ClientOptionMapper clientOptionMapper;

    public ClientMenuServiceImpl(ClientMenuMapper clientMenuMapper) {
        this.clientMenuMapper = clientMenuMapper;
    }

    @Override
    public ArrayList<ClientMenu> getAllMenu(int storeKey) {
        return clientMenuMapper.getAllMenu(storeKey);
    }

    @Override
    public ArrayList<ClientMenu> searchMenu(int storeKey, String filter, String keyword) {
        return clientMenuMapper.searchMenu(storeKey, filter, keyword);
    }

    @Override
    public void updateMenuStatus(int menuKey, String status) {
        clientMenuMapper.updateMenuStatus(menuKey, status);
    }

    @Override
    public void deleteMenu(int menuKey) {
        ArrayList<ClientOptionGroup> optionGroups = clientOptionMapper.getAllOptionGroupByMenuKey(menuKey);
        for (ClientOptionGroup optionGroup : optionGroups) {
            clientOptionMapper.deleteOptionbyOptionGroupKey(optionGroup.getOptionGroupKey());
            clientOptionMapper.deleteOptionGroup(optionGroup.getOptionGroupKey());
        }
        clientMenuMapper.deleteMenu(menuKey);
    }

    @Override
    public void deleteMenuGroup(int menuGroupKey) {
        ArrayList<ClientMenu> menus = clientMenuMapper.getAllMenuByGroupKey(menuGroupKey);
        for (ClientMenu menu : menus) {
            ArrayList<ClientOptionGroup> optionGroups = clientOptionMapper.getAllOptionGroupByMenuKey(menu.getMenuKey());
            for (ClientOptionGroup optionGroup : optionGroups) {
                clientOptionMapper.deleteOptionbyOptionGroupKey(optionGroup.getOptionGroupKey());
                clientOptionMapper.deleteOptionGroup(optionGroup.getOptionGroupKey());
            }
        }
        clientMenuMapper.deleteMenusByGroupKey(menuGroupKey);
        clientMenuMapper.deleteMenuGroup(menuGroupKey);
    }

    @Override
    public ClientMenu getMenuById(int menuKey) {
        return clientMenuMapper.getMenuById(menuKey);
    }

    @Override
    public ArrayList<ClientMenuGroup> getAllMenuGroup(int storeKey) {
        return clientMenuMapper.getAllMenuGroup(storeKey);
    }

    @Override
    public void addMenuGroup(ClientMenuGroup clientMenuGroup) {
        clientMenuMapper.addMenuGroup(clientMenuGroup);
    }

    @Override
    public void addMenu(ClientMenu clientMenu) {
        clientMenuMapper.addMenu(clientMenu);
    }

    @Override
    public void updateMenu(ClientMenu menu) {
        clientMenuMapper.updateMenu(menu);
    }
}
