/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;

import client.data.DataManipulator;
import client.data.LocalStorage;
import client.data.TranslationManager;
import client.data.WebSocketManager;
import client.popups.ServerDisconnectPopupCtrl;
import client.scenes.*;
import client.scenes.ErrorCtrl;

import client.utils.ConfigService;
import client.utils.ServerUtils;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

public class MyModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(PrimaryCtrl.class).in(Scopes.SINGLETON);
        binder.bind(RecipesWindowCtrl.class).in(Scopes.SINGLETON);
        binder.bind(ErrorCtrl.class).in(Scopes.SINGLETON);
        binder.bind(ShoppingListCtrl.class).in(Scopes.SINGLETON);
        binder.bind(IngredientsWindowCtrl.class).in(Scopes.SINGLETON);
        binder.bind(LocalStorage.class).in(Scopes.SINGLETON);
        binder.bind(DataManipulator.class).in(Scopes.SINGLETON);
        binder.bind(ServerUtils.class).in(Scopes.SINGLETON);
        binder.bind(SearchWindowCtrl.class).in(Scopes.SINGLETON);
        binder.bind(WebSocketManager.class).in(Scopes.SINGLETON);
        binder.bind(TranslationManager.class).in(Scopes.SINGLETON);
        binder.bind(ServerDisconnectPopupCtrl.class).in(Scopes.SINGLETON);
        binder.bind(ConfigService.class).in(Scopes.SINGLETON);
    }
}