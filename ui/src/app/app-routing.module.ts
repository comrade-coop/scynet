import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import { AgentsComponent } from './agents/agents.component';
import { SignalrTestComponent } from './signalr-test/signalr-test.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/agents',
    pathMatch: 'full'
  }, // opening application will redirect to search component
  {
    path: 'agents',
    component: AgentsComponent,
  },
  { path: 'agents/:id',
    component: SignalrTestComponent 
  },
  {
    path: '**',
    redirectTo: 'agents'
  } // Page not found
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: []
})
export class RoutingModule {
}
