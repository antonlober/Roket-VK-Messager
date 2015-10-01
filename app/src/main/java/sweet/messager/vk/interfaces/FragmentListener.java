package sweet.messager.vk.interfaces;

import java.util.HashMap;

public interface FragmentListener {

    void onFragment(int event);
    void onChat(HashMap<String, Object> item);
}
