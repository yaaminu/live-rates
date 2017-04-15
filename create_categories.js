#!/usr/local/bin/node

var names = ['Bills','Accommodation' ,'Food', 'Communication', 'Clothing', 'Health', 'Entertainment', 'Transport', 'Donations']


names = names.map(name => {
    return { name: name, budget: 0, budgetDuration: 2 }
})

require('fs')
    .writeFileSync('./app/src/main/assets/categories.json', JSON.stringify(names))
