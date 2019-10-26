import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../shared/auth/auth.service";
import {ActivatedRoute} from "@angular/router";
import {Subscription} from "rxjs";

@Component({
    selector: 'app-update-user',
    templateUrl: './update-user.component.html',
    styleUrls: ['./update-user.component.scss']
})
export class UpdateUserComponent implements OnInit {

    file: File = null;

    constructor (
        private authService: AuthService,
    ) {}

    ngOnInit() {}

    async fileChange(event) {
        let file: File = event.target.files[0];
        await this.authService.addProfilePicture(file, (await this.authService.userUUID))
    }


}
