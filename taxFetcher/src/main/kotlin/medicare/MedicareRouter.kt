package medicare

import generics.GenericRouter

class MedicareRouter : GenericRouter<Medicare, MedicareTable>(MedicareService(), MedicareResponse())