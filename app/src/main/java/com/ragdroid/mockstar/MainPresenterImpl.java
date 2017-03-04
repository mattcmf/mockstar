package com.ragdroid.mockstar;


import android.os.Bundle;

import com.ragdroid.mockstar.api.Pokemon;
import com.ragdroid.mockstar.api.PokemonService;
import com.ragdroid.mockstar.base.BasePresenter;
import com.ragdroid.mockstar.base.BaseSchedulerProvider;
import com.ragdroid.mockstar.contracts.MainPresenter;
import com.ragdroid.mockstar.contracts.MainScene;

import java.net.HttpURLConnection;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.HttpException;

/**
 * Created by garimajain on 05/03/17.
 */

public class MainPresenterImpl extends BasePresenter<MainScene> implements MainPresenter {

    private final PokemonService pokemonService;
    private Disposable disposable;

    @Inject
    public MainPresenterImpl(BaseSchedulerProvider schedulerProvider, PokemonService service) {
        super(schedulerProvider);
        this.pokemonService = service;
    }

    @Override
    public void onSceneAdded(MainScene scene, Bundle data) {
        super.onSceneAdded(scene, data);
        disposable = pokemonService.getPokemon("12")
                .subscribeOn(provider.io())
                .observeOn(provider.ui())
                .subscribe(new Consumer<Pokemon>() {
                    @Override
                    public void accept(@NonNull Pokemon pokemon) throws Exception {
                        if (getScene() != null) {
                            getScene().setApiText("Id : " + pokemon.getId());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        if (throwable instanceof HttpException) {
                            if (((HttpException) throwable).code() == HttpURLConnection.HTTP_NOT_FOUND) {
                                if (getScene() != null) {
                                    getScene().showErrorDialog("Lost!");
                                }
                            } else if (((HttpException) throwable).code() == HttpURLConnection.HTTP_UNAVAILABLE) {
                                if (getScene() != null) {
                                    getScene().showErrorDialog("Fire on the Server");
                                }
                            } else if (((HttpException) throwable).code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                if (getScene() != null) {
                                    getScene().showErrorDialog("You shall not pass!");
                                }
                            }
                        } else {
                            if (getScene() != null) {
                                getScene().showErrorDialog(throwable.getMessage());
                            }
                        }
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void onSceneRemoved() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onSceneRemoved();
    }


}
