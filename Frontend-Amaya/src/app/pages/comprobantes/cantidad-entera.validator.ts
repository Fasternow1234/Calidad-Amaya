import { AbstractControl, ValidationErrors } from '@angular/forms';

export function cantidadEnteraPositiva(control: AbstractControl): ValidationErrors | null {
  const value = control.value;
  if (value === null || value === '') return null;
  const cantidad = Number(value);
  return Number.isFinite(cantidad) && Number.isInteger(cantidad) && cantidad >= 1
    ? null
    : { cantidadEnteraPositiva: true };
}
