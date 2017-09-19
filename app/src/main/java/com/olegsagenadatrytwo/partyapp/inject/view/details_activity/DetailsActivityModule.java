package com.olegsagenadatrytwo.partyapp.inject.view.details_activity;

import com.olegsagenadatrytwo.partyapp.view.details_activity.DetailsActivityPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Admin on 9/13/2017.
 */
@Module
class DetailsActivityModule {

    @Provides
    DetailsActivityPresenter ProvideshomeActivityPresenter() {
        return new DetailsActivityPresenter();
    }

}
