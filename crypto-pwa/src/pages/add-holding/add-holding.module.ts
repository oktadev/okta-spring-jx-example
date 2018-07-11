import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { AddHoldingPage } from './add-holding';

@NgModule({
  declarations: [
    AddHoldingPage,
  ],
  imports: [
    IonicPageModule.forChild(AddHoldingPage),
  ],
})
export class AddHoldingPageModule {}
