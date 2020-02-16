package federalIncomeTax

import generics.GenericRouter

class FederalIncomeTaxRouter :
    GenericRouter<FederalIncomeTax, FederalIncomeTaxesTable>(FederalIncomeTaxService(), FederalIncomeTaxResponse())