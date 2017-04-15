#!/usr/local/bin/node

var names = ['Bills', 'Food', 'Communication', 'Clothing', 'Health', 'Entertainment', 'Transport', 'Donations']


names = names.map(name => {
    return { name: name, budget: 0, budgetDuration: 3 }
})

require('fs')
    .writeFileSync('./categories.json', JSON.stringify(names))
