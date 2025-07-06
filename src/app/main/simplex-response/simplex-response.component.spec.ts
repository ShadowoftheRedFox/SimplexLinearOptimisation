import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SimplexResponseComponent } from './simplex-response.component';

describe('SimplexResponseComponent', () => {
  let component: SimplexResponseComponent;
  let fixture: ComponentFixture<SimplexResponseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SimplexResponseComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SimplexResponseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
