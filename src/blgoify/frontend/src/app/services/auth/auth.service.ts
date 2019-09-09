import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginCredentials, RegisterCredentials, User } from 'src/app/models/User';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private currentUserToken_ = new BehaviorSubject('');
    private readonly dummyUser = new User('', '')
    private currentUser_ = new BehaviorSubject(this.dummyUser);

    constructor(private httpClient: HttpClient) {
    }

    async login(user: LoginCredentials) {
        const token = this.httpClient.post<UserToken>('/api/auth/signin', user);
        const it = await token.toPromise();
        console.log(`it.token: ${it.token}`);
        this.currentUserToken_.next(it.token);
        return it
    }

    register(user: RegisterCredentials) {
        return this.httpClient.post<RegisterCredentials>('/api/auth/signup', user);
    }

    private async requestUser(uuid: string) {
        const userObservable = this.httpClient.get<User>(`/api/users/${uuid}`);
        const user = await userObservable.toPromise();
        this.currentUser_.next(user);
        return user
    }

    getUserUUID(token: UserToken): Observable<UserUUID> {
        return this.httpClient.get<UserUUID>(`/api/auth/${token.token}`)
    }

    get userToken(): string {
        return this.currentUserToken_.getValue()
    }

    async getUser(uuid: string): Promise<User> {
        const cUserVal = this.currentUser_.getValue();
        if (cUserVal == this.dummyUser || cUserVal.username == '') {
            await this.requestUser(uuid)
        }
        return this.currentUser_.getValue()
    }
}


interface UserToken {
    token: string
}

interface UserUUID {
    uuid: string
}