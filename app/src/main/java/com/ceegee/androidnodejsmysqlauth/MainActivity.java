package com.ceegee.androidnodejsmysqlauth;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ceegee.androidnodejsmysqlauth.Retrofit.INodeJs;
import com.ceegee.androidnodejsmysqlauth.Retrofit.RetrofitClient;
import com.ceegee.androidnodejsmysqlauth.Retrofit.RetrofitClient;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.button.MaterialButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    INodeJs myAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    MaterialEditText edt_email, edt_password;
    Button btn_login, btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init Api
        Retrofit retrofit = RetrofitClient.getInstance();
        myAPI = retrofit.create(INodeJs.class);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);

        edt_email = findViewById(R.id.edt_email);
        edt_password = findViewById(R.id.edt_password);

        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                Login(edt_email.getText().toString(), edt_password.getText().toString());
                break;
            case R.id.btn_register:
                Register(edt_email.getText().toString(), edt_password.getText().toString());
                break;

            default:

                break;
        }
    }

    public void Login(String email, String password) {
        try {

            compositeDisposable.add(myAPI.loginUser(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e("Error Occurred", throwable.getMessage());
                            MakeToast(" " + throwable.getMessage());
                        }
                    })
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            if (s.contains("encrypted_password")) {

                                MakeToast("Login Successful");
                            } else {
                                MakeToast(" " + s);
                            }
                        }
                    }));
        }catch(Exception ex){
            MakeToast("Erro Unable to Connect "+ex.getMessage());
        }
    }

    public void Register(final String email,  final String password){
        try {

            final View enter_name_view = LayoutInflater.from(this).inflate(R.layout.enter_name_layout, null);

            new MaterialStyledDialog.Builder(this)
                    .setTitle("Register")
                    .setDescription("One More Step")
                    .setCustomView(enter_name_view)
                    .setIcon(R.drawable.ic_user_fg)
                    .setNegativeText("Cancel")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveText("Register")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //Get value from edt_name
                            MaterialEditText edt_name = enter_name_view.findViewById(R.id.edt_name);

                            compositeDisposable.add(myAPI.registerUser(email,edt_name.getText().toString(),password)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnError(new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            MakeToast(" Error Occurred");
                                        }
                                    })
                                    .subscribe(new Consumer<String>() {
                                        @Override
                                        public void accept(String s) throws Exception {
                                            MakeToast(" "+s);
                                        }
                                    }));

                        }
                    }).show();
        }catch (Exception ex){
            MakeToast("Error Occured "+ex.getMessage());
        }
    }

    private void MakeToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }
}
