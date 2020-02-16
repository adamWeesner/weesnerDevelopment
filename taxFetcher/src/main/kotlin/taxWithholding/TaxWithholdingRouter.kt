package taxWithholding

import generics.GenericRouter

class TaxWithholdingRouter :
    GenericRouter<TaxWithholding, TaxWithholdingTable>(TaxWithholdingService(), TaxWithholdingResponse())