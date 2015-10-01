package sweet.messager.vk;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import sweet.messager.vk.adapters.ColorsAdapter;
import sweet.messager.vk.interfaces.OnRecyclerItemListener;
import sweet.messager.vk.model.ColorModel;

public class ColorSelectActivity extends AppCompatActivity {

    Window window;
    ColorModel colorModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_select);
        window = getWindow();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.colors_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ColorsAdapter(new OnRecyclerItemListener() {
            @Override
            public void onClick(int position, Object object) {
                colorModel = (ColorModel) object;
                toolbar.setBackgroundColor(colorModel.toolBarColor);
                if (Build.VERSION.SDK_INT >= 21) {
                    window.setStatusBarColor(colorModel.statusBarColor);
                }
            }
        }));

        if (ApplicationName.colors != null) {
            toolbar.setBackgroundColor(ApplicationName.colors.toolBarColor);
            if (Build.VERSION.SDK_INT >= 21)
                if (Build.VERSION.SDK_INT >= 21) {
                    window.setStatusBarColor(ApplicationName.colors.statusBarColor);
                }
        }
        toolbar.setNavigationIcon(R.mipmap.ic_toolbar_back);
        toolbar.setTitle("Выбор цвета");
        toolbar.inflateMenu(R.menu.menu_done);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_done:
                        if (colorModel != null) {
                            ApplicationName.setAppColor(colorModel);
                            setResult(RESULT_OK, new Intent());
                            finish();
                        }
                        break;
                }
                return true;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

}
