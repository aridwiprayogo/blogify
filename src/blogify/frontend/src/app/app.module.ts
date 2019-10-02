import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http'
import { FormsModule } from '@angular/forms'

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './components/login/login.component';
import { ProfileComponent } from './components/profile/profile.component';
import { HomeComponent } from './components/home/home.component';
import { RouterModule } from "@angular/router";
import { NewArticleComponent } from './components/newarticle/new-article.component';
import { ShowArticleComponent } from './components/show-article/show-article.component';
import { ShowAllArticlesComponent } from './components/show-all-articles/show-all-articles.component';
import { CommentComponent } from './components/comment/comment.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { DarkThemeDirective } from './directives/dark-theme/dark-theme.directive';
import { AngularFontAwesomeModule } from 'angular-font-awesome';
import { CompactDirective } from './directives/compact/compact.directive';


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        ProfileComponent,
        HomeComponent,
        NewArticleComponent,
        ShowArticleComponent,
        ShowAllArticlesComponent,
        CommentComponent,
        NavbarComponent,
        DarkThemeDirective,
        CompactDirective
    ],
    imports: [
        BrowserModule,
        RouterModule,
        AppRoutingModule,
        HttpClientModule,
        FormsModule,
        AngularFontAwesomeModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
