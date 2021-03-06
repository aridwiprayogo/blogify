import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { StaticContentService } from '../../../services/static/static-content.service';
import { StaticFile } from "../../../models/Static";
import { faUser } from '@fortawesome/free-regular-svg-icons';

@Component({
    selector: 'app-profile-picture',
    templateUrl: './profile-picture.component.html',
    styleUrls: ['./profile-picture.component.scss']
})
export class ProfilePictureComponent implements OnInit, OnChanges {

    @Input() pfpFile: StaticFile;
    @Input() emSize: number = 3;
    @Input() displayedVertically: boolean = false;

    sourceUrl: string | null = null;

    faUser = faUser;

    constructor(private staticContentService: StaticContentService) {}

    ngOnInit() {}

    ngOnChanges(changes: SimpleChanges): void {
        this.sourceUrl = this.pfpFile.fileId ? this.staticContentService.urlFor(this.pfpFile) : null;
    }

}
