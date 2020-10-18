export const getLowerMapFromArrangement = (arrangement) => {
    const mapData = {}
    arrangement.forEach(column => {
        const activeName = column[0][0].data.name
        if (column[1] != null) {
            column[1].forEach(card => {
                let newData = mapData[activeName] ?? []
                newData.push(card.data.name)
                mapData[activeName] = newData
            })
        }
    })
    return mapData;
}