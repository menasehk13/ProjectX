package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OtpTextView;

public class Verify extends AppCompatActivity {
   OtpTextView verify_code;
    Button verify;
    String verificationid;
    String Phone_Number;
    FirebaseAuth auth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        verify_code=findViewById(R.id.verifycode);
        verify=findViewById(R.id.btverify);
        auth=FirebaseAuth.getInstance();
        Phone_Number=getIntent().getStringExtra("phonenumber");
        sendVerificationCode(Phone_Number);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code=verify_code.getOTP();
                if(code.isEmpty()||code.length()<6){
                    verify_code.showError();
                    verify_code.setFocusable(true);
                }
                Verifycode(code);
            }
        });
    }

    private void sendVerificationCode(String phone_number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+"+phone_number,
                60,
                TimeUnit.MILLISECONDS,
                TaskExecutors.MAIN_THREAD,
                callback
        );
    }
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callback=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
         @Override
         public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
             super.onCodeSent(s, forceResendingToken);
             verificationid=s;
         }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code=phoneAuthCredential.getSmsCode();
            if(code!=null){
                verify_code.setOTP(code);
                Verifycode(code);

            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

        }
    };

    private void Verifycode(String code) {
        PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(verificationid,code);
        signInwithCredetial(phoneAuthCredential);
    }

    private void signInwithCredetial(PhoneAuthCredential phoneAuthCredential) {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
           if (task.isSuccessful()){
               FirebaseUser user = task.getResult().getUser();
               long creationTimestamp = user.getMetadata().getCreationTimestamp();
               long lastSignInTimestamp = user.getMetadata().getLastSignInTimestamp();
               if (creationTimestamp == lastSignInTimestamp) {
                   Intent intent=new Intent(Verify.this,SignUp.class);
                   intent.putExtra("UserPhone",Phone_Number);
                   startActivity(intent);
                   finish();
               } else {
                   Intent intent2=new Intent(Verify.this,Main.class);
                   startActivity(intent2);
                   finish();
               }
           }else {
               Toast.makeText(Verify.this,task.getException().toString(), Toast.LENGTH_SHORT).show();
           }
            }
        });
    }
}
