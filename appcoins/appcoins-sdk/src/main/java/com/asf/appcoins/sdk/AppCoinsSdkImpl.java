package com.asf.appcoins.sdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.asf.appcoins.sdk.entity.Transaction;
import com.asf.appcoins.sdk.entity.Transaction.Status;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.util.Formatter;
import java.util.concurrent.TimeUnit;

/**
 * Created by neuro on 01-03-2018.
 */

final class AppCoinsSdkImpl implements AppCoinsSdk {

  private static final int DEFAULT_PERIOD = 5;
  private static final int DECIMALS = 18;

  private static final int DEFAULT_REQUEST_CODE = 3423;
  private final int period;
  private final AsfWeb3j asfWeb3j;
  private final Scheduler scheduler;
  private final SkuManager skuManager;
  private final int networkId;

  AppCoinsSdkImpl(AsfWeb3j asfWeb3j) {
    this(asfWeb3j, DEFAULT_PERIOD, Schedulers.io(), new SkuManager(), false);
  }

  AppCoinsSdkImpl(AsfWeb3j asfWeb3j, int period, Scheduler scheduler, SkuManager skuManager,
      boolean debug) {
    this.asfWeb3j = asfWeb3j;
    this.period = period;
    this.scheduler = scheduler;
    this.skuManager = skuManager;
    this.networkId = debug ? 3 : 1;
  }

  @Override public Observable<Transaction> getTransaction(String txHash) {
    return Observable.interval(period, TimeUnit.SECONDS, scheduler)
        .timeInterval()
        .switchMap(scan -> asfWeb3j.getTransactionByHash(txHash))
        .takeUntil(transaction -> transaction.getStatus() == Status.PENDING);
  }

  @Override public void buy(String sku, Activity activity) {
    Intent intent = new Intent(Intent.ACTION_VIEW);

    BigDecimal amount = skuManager.getSkuAmount(sku);
    BigDecimal total = amount.multiply(BigDecimal.TEN.pow(DECIMALS));

    intent.setData(buildUri("0xab949343E6C369C6B17C7ae302c1dEbD4B7B61c3", networkId,
        "0x4fbcc5ce88493c3d9903701c143af65f54481119", total));
    activity.startActivityForResult(intent, DEFAULT_REQUEST_CODE);
  }

  Uri buildUri(String contractAddress, int networkId, String developerAddress, BigDecimal amount) {
    return buildUri(contractAddress, networkId, developerAddress, amount);
  }

  String buildUriString(String contractAddress, int networkId, String developerAddress,
      BigDecimal amount) {
    StringBuilder stringBuilder = new StringBuilder(4);
    Formatter formatter = new Formatter(stringBuilder);
    formatter.format("ethereum:%s@%d/transfer?address=%s&uint256=%s", contractAddress, networkId,
        developerAddress, amount.toString());

    return stringBuilder.toString();
  }
}