import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SignalrTestComponent } from './signalr-test.component';

describe('SignalrTestComponent', () => {
  let component: SignalrTestComponent;
  let fixture: ComponentFixture<SignalrTestComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SignalrTestComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SignalrTestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
